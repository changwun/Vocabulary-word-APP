package com.example.quiz.service;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.QEvent;
import com.example.quiz.entity.QRaffle;
import com.example.quiz.entity.QUser;
import com.example.quiz.repository.EventRepository;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.UserRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final JPAQueryFactory queryFactory;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RaffleRepository raffleRepository;

    /**
     * 관리자 대시보드용 통계 조회 (Querydsl 활용)
     */
    @Transactional(readOnly = true)
    public QuizDto.AdminDashboardResponse getDashboardStats() {
        QUser user = QUser.user;
        QRaffle raffle = QRaffle.raffle;
        QEvent event = QEvent.event;

        long totalUsers = queryFactory.selectFrom(user).fetchCount();
        long totalRafflesUsed = queryFactory.selectFrom(raffle).fetchCount();
        long activeEvents = queryFactory.selectFrom(event).where(event.active.isTrue()).fetchCount();

        // 이벤트별 참여 인원 통계
        List<Tuple> stats = queryFactory
                .select(event.id, event.title, raffle.count(), event.drawn)
                .from(event)
                .leftJoin(raffle).on(raffle.event.eq(event))
                .groupBy(event.id)
                .fetch();

        List<QuizDto.EventWinnerStat> eventStats = stats.stream()
                .map(tuple -> QuizDto.EventWinnerStat.builder()
                        .eventId(tuple.get(event.id))
                        .eventTitle(tuple.get(event.title))
                        .participantCount(tuple.get(raffle.count()))
                        .isDrawn(Boolean.TRUE.equals(tuple.get(event.drawn)))
                        .build())
                .collect(Collectors.toList());

        return QuizDto.AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalRafflesUsed(totalRafflesUsed)
                .activeEventsCount(activeEvents)
                .eventStats(eventStats)
                .build();
    }
}
