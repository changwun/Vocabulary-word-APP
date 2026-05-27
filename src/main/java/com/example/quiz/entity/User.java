package com.example.quiz.entity;

import com.example.quiz.dto.QuizDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private int raffleCount = 0; // 보유 응모권 개수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizDto.QuizMode quizMode = QuizDto.QuizMode.EN_TO_KO; // 기본값

    @Builder
    public User(String username, String email, String password, UserRole role, QuizDto.QuizMode quizMode) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.quizMode = quizMode != null ? quizMode : QuizDto.QuizMode.EN_TO_KO;
        this.raffleCount = 0;
    }

    public void updateQuizMode(QuizDto.QuizMode quizMode) {
        this.quizMode = quizMode;
    }

    public void addRaffle() {
        this.raffleCount++;
    }

    public void useRaffle() {
        if (this.raffleCount <= 0) {
            throw new IllegalStateException("사용 가능한 응모권이 없습니다.");
        }
        this.raffleCount--;
    }
}
