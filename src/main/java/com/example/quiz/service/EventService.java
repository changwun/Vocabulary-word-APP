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
     * 현재 진행 중이거나 앞으로 예정된 이벤트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Event> getVisibleEvents() {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();

        return queryFactory
                .selectFrom(event)
                .where(
                    event.active.isTrue(),
                    event.endDate.after(now) // 아직 끝나지 않은 이벤트만 노출
                )
                .orderBy(event.startDate.asc())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Event> getActiveEvents() {
        return getVisibleEvents(); // UI 요구사항에 맞춰 예정된 이벤트도 포함
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
