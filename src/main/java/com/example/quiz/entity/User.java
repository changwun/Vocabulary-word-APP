package com.example.quiz.entity;

import com.example.quiz.dto.QuizDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

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
    @Column(nullable = true, length = 100) // 소셜 로그인의 경우 비어있을 수 있음
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private int raffleCount = 0; // 보유 응모권 개수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizDto.QuizMode quizMode = QuizDto.QuizMode.EN_TO_KO; // 기본값

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    private LocalTime notificationTime;

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    @Builder
    public User(String username, String email, String phoneNumber, String password, UserRole role, 
                QuizDto.QuizMode quizMode, boolean privacyPolicyAgreed, AuthProvider provider, String providerId,
                LocalTime notificationTime, Boolean notificationEnabled) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.quizMode = quizMode != null ? quizMode : QuizDto.QuizMode.EN_TO_KO;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.raffleCount = 0;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.notificationTime = notificationTime;
        this.notificationEnabled = notificationEnabled != null ? notificationEnabled : true;
    }

    public void updateQuizMode(QuizDto.QuizMode quizMode) {
        this.quizMode = quizMode;
    }

    public void updateProfile(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public void updateNotificationSettings(LocalTime notificationTime, boolean notificationEnabled) {
        this.notificationTime = notificationTime;
        this.notificationEnabled = notificationEnabled;
    }

    public void linkSocialAccount(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
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
