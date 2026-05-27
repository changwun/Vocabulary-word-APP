package com.example.quiz.service;

import com.example.quiz.entity.Event;
import com.example.quiz.entity.QEvent;
import com.example.quiz.repository.EventRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final JPAQueryFactory queryFactory;
    private final EventRepository eventRepository;

    /**
     * Querydsl을 사용하여 현재 진행 중인 이벤트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Event> getActiveEvents() {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();

        return queryFactory
                .selectFrom(event)
                .where(
                    event.active.isTrue(),
                    event.startDate.before(now),
                    event.endDate.after(now)
                )
                .fetch();
    }

    @Transactional
    public void createEvent(String title, String description, String prize, LocalDateTime start, LocalDateTime end) {
        eventRepository.save(Event.builder()
                .title(title)
                .description(description)
                .prize(prize)
                .startDate(start)
                .endDate(end)
                .build());
    }
}
