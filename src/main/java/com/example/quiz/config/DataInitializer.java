package com.example.quiz.config;

import com.example.quiz.entity.Event;
import com.example.quiz.entity.Word;
import com.example.quiz.repository.EventRepository;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.WinnerRepository;
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
    private final WinnerRepository winnerRepository;

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

        // 이벤트 데이터 초기화 (미래 데이터 포함하여 풍성하게 구성)
        log.info("Refreshing event data with schedule...");
        try {
            // 자식 데이터(Winner, Raffle)를 먼저 지워야 외래 키 오류가 나지 않습니다.
            winnerRepository.deleteAll();
            raffleRepository.deleteAll();
            eventRepository.deleteAll();
            
            LocalDateTime now = LocalDateTime.now();

            List<Event> defaultEvents = Arrays.asList(
                // 1. 현재 진행 중인 이벤트
                Event.builder()
                    .title("오늘의 달콤한 충전 🍬")
                    .description("오늘의 퀴즈 퍼펙트 달성 시 추첨을 통해 간식을 드립니다!")
                    .prize("편의점 과자 기프티콘")
                    .startDate(now.minusHours(12))
                    .endDate(now.plusHours(12))
                    .active(true)
                    .build(),
                Event.builder()
                    .title("주간 커피 타임 (이번 주) ☕")
                    .description("매일 퀴즈 풀고 주간 행운의 주인공이 되어보세요! (10명)")
                    .prize("스타벅스 아메리카노 (10명)")
                    .startDate(now.minusDays(1))
                    .endDate(now.plusDays(6))
                    .active(true)
                    .build(),
                
                // 2. 내일 시작될 예정 이벤트 (미리보기용)
                Event.builder()
                    .title("내일의 간식 타임 🍩")
                    .description("내일도 퀴즈 풀고 던킨 도너츠 받아가세요!")
                    .prize("던킨 도너츠 1개")
                    .startDate(now.plusDays(1).withHour(0).withMinute(0))
                    .endDate(now.plusDays(1).withHour(23).withMinute(59))
                    .active(true)
                    .build(),
                Event.builder()
                    .title("다음 주 스페셜 이벤트 🎁")
                    .description("다음 주에는 더 특별한 경품이 기다리고 있습니다.")
                    .prize("문화상품권 1만원권")
                    .startDate(now.plusDays(7))
                    .endDate(now.plusDays(14))
                    .active(true)
                    .build()
            );
            
            eventRepository.saveAll(defaultEvents);
            log.info("Successfully refreshed scheduled events.");
        } catch (Exception e) {
            log.error("Failed to refresh events: {}", e.getMessage());
        }
    }
}
