package com.example.quiz.service;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.User;
import com.example.quiz.entity.Word;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final StringRedisTemplate redisTemplate;
    private final QuizProcessor quizProcessor;
    private final NaverDictionaryClient naverDictionaryClient;

    public static final String QUIZ_KEY_PREFIX = "quiz:assignment:";
    public static final String ATTEMPT_KEY_PREFIX = "quiz:attempt:";
    public static final String DICT_CACHE_PREFIX = "dict:verify:";

    @Scheduled(cron = "0 0 0 * * *")
    public void assignDailyWords() {
        LocalDate todayDate = LocalDate.now();
        String lockKey = "lock:assign-words:" + todayDate;
        
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", 1, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(acquired)) return;

        try {
            List<Word> dailyWords = wordRepository.findRandomWords(5);
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
    public List<QuizDto.QuestionResponse> getDailyQuizzes(Long userId) {
        String todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String attemptKey = ATTEMPT_KEY_PREFIX + todayStr + ":" + userId;

        if (redisTemplate.hasKey(attemptKey)) {
            throw new IllegalStateException("오늘은 이미 퀴즈에 참여하셨습니다. 내일 다시 도전해 주세요!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        QuizDto.QuizMode mode = user.getQuizMode();

        String key = QUIZ_KEY_PREFIX + todayStr + ":" + userId;
        String wordIdsStr = redisTemplate.opsForValue().get(key);
        List<Word> words;
        
        if (wordIdsStr == null) {
            words = wordRepository.findRandomWords(5);
            if (words.isEmpty()) throw new IllegalStateException("단어 데이터가 없습니다.");

            wordIdsStr = words.stream()
                    .map(w -> String.valueOf(w.getId()))
                    .collect(Collectors.joining(","));

            redisTemplate.opsForValue().set(key, wordIdsStr, 24, TimeUnit.HOURS);
        } else {
            List<Long> wordIds = Arrays.stream(wordIdsStr.split(","))
                    .limit(5)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            words = wordRepository.findAllById(wordIds);
            
            Map<Long, Word> wordMap = words.stream().collect(Collectors.toMap(Word::getId, w -> w));
            words = wordIds.stream()
                    .map(wordMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return words.stream()
                .map(w -> QuizDto.QuestionResponse.builder()
                        .wordId(w.getId())
                        .question(mode == QuizDto.QuizMode.EN_TO_KO ? w.getEnglish() : w.getKorean())
                        .mode(mode)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizDto.QuizResultResponse completeQuiz(Long userId, QuizDto.QuizCompleteRequest request) {
        LocalDate now = LocalDate.now();
        String todayStr = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String attemptKey = ATTEMPT_KEY_PREFIX + todayStr + ":" + userId;
        String quizKey = QUIZ_KEY_PREFIX + todayStr + ":" + userId;

        Boolean isFirstAttempt = redisTemplate.opsForValue()
                .setIfAbsent(attemptKey, "PROCESSING", 24, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstAttempt)) {
            throw new IllegalStateException("이미 오늘 퀴즈에 참여하셨습니다.");
        }

        String assignedIdsStr = redisTemplate.opsForValue().get(quizKey);
        if (assignedIdsStr == null) {
            redisTemplate.delete(attemptKey);
            throw new IllegalStateException("오늘의 퀴즈를 할당받지 않았습니다.");
        }
        
        Set<Long> assignedIds = Arrays.stream(assignedIdsStr.split(","))
                .limit(5)
                .map(Long::parseLong).collect(Collectors.toSet());
        
        List<Long> submittedWordIds = request.getAnswers().stream()
                .map(QuizDto.AnswerRequest::getWordId)
                .collect(Collectors.toList());

        if (submittedWordIds.size() != 5 || !assignedIds.equals(new HashSet<>(submittedWordIds))) {
            redisTemplate.delete(attemptKey);
            throw new IllegalArgumentException("제출된 단어 정보가 올바르지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        QuizDto.QuizMode userMode = user.getQuizMode();

        List<Word> words = wordRepository.findAllById(submittedWordIds);
        Map<Long, Word> wordMap = words.stream().collect(Collectors.toMap(Word::getId, w -> w));
        List<QuizDto.WrongAnswerDetail> wrongDetails = new ArrayList<>();

        for (QuizDto.AnswerRequest answerReq : request.getAnswers()) {
            Word word = wordMap.get(answerReq.getWordId());
            if (word == null) continue;

            String question = (userMode == QuizDto.QuizMode.EN_TO_KO) ? word.getEnglish() : word.getKorean();
            String dbAnswer = (userMode == QuizDto.QuizMode.EN_TO_KO) ? word.getKorean() : word.getEnglish();
            String userAnswer = answerReq.getAnswer() != null ? answerReq.getAnswer().trim() : "";
            
            // 1단계: DB 정답과 정확히 일치하는지 확인
            if (dbAnswer != null && dbAnswer.trim().equalsIgnoreCase(userAnswer)) {
                continue;
            }

            // 2단계: 네이버 사전 API를 통한 유연한 채점 (유의어 인정)
            if (isSimilarAnswer(question, userAnswer)) {
                continue;
            }

            // 둘 다 실패 시 오답 처리
            wrongDetails.add(QuizDto.WrongAnswerDetail.builder()
                    .wordId(word.getId())
                    .question(question)
                    .correctAnswer(dbAnswer)
                    .userAnswer(userAnswer.isEmpty() ? "(빈칸)" : userAnswer)
                    .build());
        }

        boolean isSuccess = wrongDetails.isEmpty();

        if (isSuccess) {
            user.addRaffle();
            userRepository.save(user);
            redisTemplate.opsForValue().set(attemptKey, "SUCCESS", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().set(attemptKey, "FAILED", 24, TimeUnit.HOURS);
        }

        return QuizDto.QuizResultResponse.builder()
                .success(isSuccess)
                .wrongDetails(wrongDetails)
                .message(isSuccess ? "축하합니다! 유의어까지 인정되어 모든 정답을 맞췄습니다." : "아쉽게도 틀린 문제가 있습니다. 정답을 확인해 보세요!")
                .raffleCount(user.getRaffleCount())
                .build();
    }

    /**
     * 유의어 검증 로직 (Redis 캐싱 포함)
     */
    private boolean isSimilarAnswer(String question, String userAnswer) {
        if (userAnswer.isEmpty()) return false;

        String cacheKey = DICT_CACHE_PREFIX + question + ":" + userAnswer;
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResult != null) {
            return Boolean.parseBoolean(cachedResult);
        }

        // 캐시 없으면 네이버 API 호출
        boolean isSimilar = naverDictionaryClient.checkSimilarity(question, userAnswer);
        
        // 결과 캐싱 (24시간)
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(isSimilar), 24, TimeUnit.HOURS);
        
        return isSimilar;
    }
}
