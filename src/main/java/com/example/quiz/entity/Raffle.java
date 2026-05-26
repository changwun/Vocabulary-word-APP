package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import java.time.LocalDate;

@Entity
@Table(name = "raffles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "raffle_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Raffle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "raffle_date", nullable = false)
    private LocalDate raffleDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Raffle(User user, LocalDate raffleDate) {
        this.user = user;
        this.raffleDate = raffleDate != null ? raffleDate : LocalDate.now();
    }
}
