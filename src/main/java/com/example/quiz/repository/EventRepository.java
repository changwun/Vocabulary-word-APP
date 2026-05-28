package com.example.quiz.repository;

import com.example.quiz.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByActiveTrueAndEndDateBefore(LocalDateTime now);
    List<Event> findAllByOrderByEndDateDesc(); // 최신 종료순 조회용
}
