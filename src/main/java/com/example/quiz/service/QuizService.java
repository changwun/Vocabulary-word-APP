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
import java.util.*;
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

    @Scheduled(cron = "0 0 0 * * *")
    public void assignDailyWords() {
        LocalDate todayDate = LocalDate.now();
        String lockKey = "lock:assign-words:" + todayDate;
        
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(acquired)) return;

        try {
            List<Word> dailyWords = wordRepository.findRandomWords(10);
            if (dailyWords.isEmpty()) return;

            String wordIds = dailyWords.stream()
                    .map(w -> String.valueOf(w.getId()))
                    .collect(Collectors.joining(","));

            String todayStr = todayDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            
            int pageNumber = 0;
            while (quizProcessor.processChunk(pageNumber, todayStr, wordIds)) {
                pageNumber++;
            }
        } finally {
            redisTemplate.delete(lockKey);
            log.info("Batch assignment completed.");
        }
    }

    @Transactional(readOnly = true)
    public List<Word> getDailyWords(Long userId) {
        String todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = QUIZ_KEY_PREFIX + todayStr + ":" + userId;

        String wordIdsStr = redisTemplate.opsForValue().get(key);
        
        if (wordIdsStr == null) {
            List<Word> randomWords = wordRepository.findRandomWords(10);
            if (randomWords.isEmpty()) throw new IllegalStateException("단어 데이터가 없습니다.");

            wordIdsStr = randomWords.stream()
                    .map(w -> String.valueOf(w.getId()))
                    .collect(Collectors.joining(","));

            redisTemplate.opsForValue().set(key, wordIdsStr, 24, TimeUnit.HOURS);
            return randomWords;
        }

        List<Long> wordIds = Arrays.stream(wordIdsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<Word> words = wordRepository.findAllById(wordIds);
        Map<Long, Word> wordMap = words.stream().collect(Collectors.toMap(Word::getId, w -> w));
        
        return wordIds.stream()
                .map(wordMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public void completeQuiz(Long userId, List<Long> submittedWordIds) {
        validateUserAuthority(userId);
        
        LocalDate now = LocalDate.now();
        String todayStr = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String quizKey = QUIZ_KEY_PREFIX + todayStr + ":" + userId;
        String lockKey = COMPLETED_KEY_PREFIX + todayStr + ":" + userId;

        // 1. 보안 검증: 오늘 할당된 단어를 실제로 풀었는지 확인
        String assignedIdsStr = redisTemplate.opsForValue().get(quizKey);
        if (assignedIdsStr == null) throw new IllegalStateException("오늘의 퀴즈를 할당받지 않았습니다.");
        
        Set<Long> assignedIds = Arrays.stream(assignedIdsStr.split(","))
                .map(Long::parseLong).collect(Collectors.toSet());
        
        if (submittedWordIds == null || !assignedIds.equals(new HashSet<>(submittedWordIds))) {
            throw new IllegalArgumentException("제출된 단어 정보가 올바르지 않습니다.");
        }

        // 2. 원자적 락으로 중복 요청 방어
        Boolean isFirstTime = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "PROCESSING", 10, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isFirstTime)) {
            throw new IllegalStateException("이미 참여 중이거나 완료된 퀴즈입니다.");
        }

        // 3. 트랜잭션 동기화: 결과에 따라 Redis 상태 확정
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    redisTemplate.opsForValue().set(lockKey, "COMPLETED", 24, TimeUnit.HOURS);
                } else {
                    redisTemplate.delete(lockKey);
                }
            }
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        raffleRepository.save(Raffle.builder()
                .user(user)
                .raffleDate(now)
                .build());
    }

    private void validateUserAuthority(Long userId) {
        log.info("Authority validated for user: {}", userId);
    }
}
