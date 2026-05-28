package com.example.quiz.entity;

import com.example.quiz.dto.QuizDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_user_phone", columnNames = "phoneNumber")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // 중복 허용

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean privacyPolicyAgreed; // 개인정보 수집 동의 여부

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
    public User(String username, String email, String phoneNumber, String password, UserRole role, QuizDto.QuizMode quizMode, boolean privacyPolicyAgreed) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.quizMode = quizMode != null ? quizMode : QuizDto.QuizMode.EN_TO_KO;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
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

    public boolean isAdmin() {
        return this.role == UserRole.ROLE_ADMIN;
    }
}
