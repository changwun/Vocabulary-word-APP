package com.example.quiz.config;

import com.example.quiz.entity.Word;
import com.example.quiz.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;

    @Override
    public void run(String... args) {
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
        } else {
            log.info("Word data already exists. Skipping initialization.");
        }
    }
}
