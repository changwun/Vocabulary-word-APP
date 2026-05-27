package com.example.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "raffles") // 제약사항 제거 (이벤트마다 여러번 응모 가능하므로)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Raffle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "raffle_date", nullable = false)
    private LocalDate raffleDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Raffle(User user, Event event, LocalDate raffleDate) {
        this.user = user;
        this.event = event;
        this.raffleDate = raffleDate != null ? raffleDate : LocalDate.now();
    }
}
