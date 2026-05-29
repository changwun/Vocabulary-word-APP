package com.example.quiz.repository;

import com.example.quiz.entity.Winner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WinnerRepository extends JpaRepository<Winner, Long> {
    List<Winner> findAllByEventId(Long eventId);
    List<Winner> findAllByWonAtBetween(LocalDateTime start, LocalDateTime end);
    List<Winner> findAllByOrderByWonAtDesc();
}
