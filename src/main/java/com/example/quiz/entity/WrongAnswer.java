package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wrong_answers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_word", columnNames = {"user_id", "word_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WrongAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(nullable = false)
    private int wrongCount;

    @Column(nullable = false)
    private LocalDateTime lastAttemptAt;

    @Builder
    public WrongAnswer(User user, Word word) {
        this.user = user;
        this.word = word;
        this.wrongCount = 1;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void incrementCount() {
        this.wrongCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }
}
