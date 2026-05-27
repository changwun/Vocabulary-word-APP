package com.example.quiz.config;

import com.example.quiz.entity.Event;
import com.example.quiz.entity.Word;
import com.example.quiz.repository.EventRepository;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;
    private final EventRepository eventRepository;
    private final RaffleRepository raffleRepository;

    @Override
    public void run(String... args) {
        // 단어 데이터 초기화
        if (wordRepository.count() == 0) {
            log.info("Initializing default word data...");
            List<Word> defaultWords = Arrays.asList(
                new Word("apple", "사과"),
                new Word("banana", "바나나"),
                new Word("computer", "컴퓨터"),
                new Word("developer", "개발자"),
                new Word("spring", "봄/스프링"),
                new Word("database", "데이터베이스"),
                new Word("network", "네트워크"),
                new Word("security", "보안"),
                new Word("variable", "변수"),
                new Word("function", "함수"),
                new Word("architecture", "아키텍처"),
                new Word("interface", "인터페이스"),
                new Word("abstraction", "추상화"),
                new Word("encapsulation", "캡슐화"),
                new Word("inheritance", "상속"),
                new Word("polymorphism", "다형성"),
                new Word("algorithm", "알고리즘"),
                new Word("performance", "성능"),
                new Word("scalability", "확장성"),
                new Word("optimization", "최적화"),
                new Word("cloud", "클라우드"),
                new Word("container", "컨테이너")
            );
            wordRepository.saveAll(defaultWords);
            log.info("Successfully seeded {} words.", defaultWords.size());
        }

        // 이벤트 데이터 초기화 (기존 데이터 삭제 후 갱신)
        // 외래 키 제약 조건(FK) 때문에 Raffle 데이터를 먼저 지워야 합니다.
        log.info("Refreshing real-world event data...");
        try {
            raffleRepository.deleteAll(); 
            eventRepository.deleteAll(); 
            
            List<Event> defaultEvents = Arrays.asList(
                Event.builder()
                    .title("주간 커피 타임 ☕")
                    .description("일주일 동안 퀴즈를 꾸준히 풀어보세요! 추첨을 통해 10분께 커피를 드립니다.")
                    .prize("스타벅스 아메리카노 (10명)")
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusDays(7))
                    .active(true)
                    .build(),
                Event.builder()
                    .title("오늘의 달콤한 충전 🍬")
                    .description("오늘의 퀴즈를 완벽하게 맞춘 분들 중 추첨을 통해 달콤한 선물을 드립니다!")
                    .prize("편의점 과자/사탕 기프티콘")
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusDays(1))
                    .active(true)
                    .build()
            );
            eventRepository.saveAll(defaultEvents);
            log.info("Successfully refreshed events.");
        } catch (Exception e) {
            log.error("Failed to refresh events: {}", e.getMessage());
        }
    }
}
