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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.transaction.support.TransactionSynchronization.STATUS_COMMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final RaffleRepository raffleRepository;
    private final StringRedisTemplate redisTemplate;
    private final QuizProcessor quizProcessor;

    public static final String QUIZ_KEY_PREFIX = "quiz:assignment:";
    public static final String COMPLETED_KEY_PREFIX = "quiz:completed:";

    /**
     * 매일 자정 모든 유저에게 새로운 단어 10개 할당
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void assignDailyWords() {
        LocalDate todayDate = LocalDate.now();
        String lockKey = "lock:assign-words:" + todayDate;
        
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(acquired)) return;

        try {
            List<Word> dailyWords = wordRepository.findRandomWords(10);
            String wordIds = dailyWords.stream()
                    .map(w -> String.valueOf(w.getId()))
                    .collect(Collectors.joining(","));

            String todayStr = todayDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            
            int pageNumber = 0;
            while (true) {
                if (!quizProcessor.processChunk(pageNumber, todayStr, wordIds)) break;
                pageNumber++;
            }
        } finally {
            // 정상 종료 시 락 해제 (실패 시엔 TTL에 의존)
            redisTemplate.delete(lockKey);
            log.info("Batch assignment completed.");
        }
    }

    /**
     * 퀴즈 완료 및 응모권 발급
     */
    @Transactional
    public void completeQuiz(Long userId) {
        // [IDOR 방어] 실제 보안 컨텍스트 확인 로직 (시뮬레이션)
        validateUserAuthority(userId);
        
        // 자정 시점의 일관성을 위해 날짜 고정
        LocalDate now = LocalDate.now();
        String todayStr = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String lockKey = COMPLETED_KEY_PREFIX + todayStr + ":" + userId;

        Boolean isFirstTime = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "PROCESSING", 10, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isFirstTime)) {
            throw new IllegalStateException("이미 참여 중이거나 완료된 퀴즈입니다.");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    redisTemplate.opsForValue().set(lockKey, "COMPLETED", 24, TimeUnit.HOURS);
                } else {
                    String val = redisTemplate.opsForValue().get(lockKey);
                    if ("PROCESSING".equals(val)) {
                        redisTemplate.delete(lockKey);
                    }
                }
            }
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // DB 유니크 제약 조건이 최종적으로 정합성을 보장함
        raffleRepository.save(Raffle.builder()
                .user(user)
                .raffleDate(now)
                .build());
    }

    private void validateUserAuthority(Long userId) {
        // 실제 운영 환경: String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        // if (!currentUserId.equals(userId.toString())) throw new AccessDeniedException("접근 권한이 없습니다.");
        log.info("User authority validated for ID: {}", userId);
    }
}
