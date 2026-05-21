package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String english;

    @Column(nullable = false)
    private String korean;

    @Builder
    public Word(String english, String korean) {
        this.english = english;
        this.korean = korean;
    }
}
