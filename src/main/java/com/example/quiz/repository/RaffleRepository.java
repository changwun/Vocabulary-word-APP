package com.example.quiz.repository;

import com.example.quiz.entity.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaffleRepository extends JpaRepository<Raffle, Long> {
}
