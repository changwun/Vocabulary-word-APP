package com.example.quiz.service;

import com.example.quiz.entity.Raffle;
import com.example.quiz.entity.User;
import com.example.quiz.entity.Word;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    // TODO: 실무 적용 시 대규모 트래픽을 위한 파티셔닝 전략 검토 필요
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final RaffleRepository raffleRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String QUIZ_KEY_PREFIX = "quiz:assignment:";
    private static final String COMPLETED_KEY_PREFIX = "quiz:completed:";

    /**
     * 매일 자정 모든 유저에게 새로운 단어 10개 할당 (Redis 저장)
     * 대용량 트래픽 고려 시 Batch Update나 온디맨드 할당 권장
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void assignDailyWords() {
        List<Word> dailyWords = wordRepository.findRandomWords(10);
        String wordIds = dailyWords.stream()
                .map(w -> String.valueOf(w.getId()))
                .reduce((a, b) -> a + "," + b).orElse("");

        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // 실무에서는 유저가 많을 경우 페이징 처리 필수
        userRepository.findAll().forEach(user -> {
            String key = QUIZ_KEY_PREFIX + today + ":" + user.getId();
            redisTemplate.opsForValue().set(key, wordIds, 24, TimeUnit.HOURS);
        });
        log.info("Midnight word assignment completed for all users.");
    }

    /**
     * 퀴즈 완료 및 응모권 발급
     */
    @Transactional
    public void completeQuiz(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String lockKey = COMPLETED_KEY_PREFIX + today + ":" + userId;

        // 1. Redis를 이용한 중복 참여 방지 (Atomic SETNX)
        Boolean isFirstTime = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "COMPLETED", 24, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstTime)) {
            throw new IllegalStateException("이미 오늘의 퀴즈에 참여하여 응모권을 받았습니다.");
        }

        // 2. 응모권 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Raffle raffle = Raffle.builder()
                .user(user)
                .build();

        raffleRepository.save(raffle);
        log.info("Raffle issued for user: {}", userId);
    }
}
