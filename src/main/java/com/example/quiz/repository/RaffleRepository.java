package com.example.quiz.repository;

import com.example.quiz.entity.Event;
import com.example.quiz.entity.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaffleRepository extends JpaRepository<Raffle, Long> {
    List<Raffle> findAllByEvent(Event event);
}
