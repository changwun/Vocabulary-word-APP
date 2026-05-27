package com.example.quiz.service;

import com.example.quiz.entity.Event;
import com.example.quiz.entity.Raffle;
import com.example.quiz.entity.User;
import com.example.quiz.repository.EventRepository;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaffleService {

    private final UserRepository userRepository;
    private final RaffleRepository raffleRepository;
    private final EventRepository eventRepository;
    private final RedissonClient redissonClient;

    /**
     * 특정 이벤트에 응모권 사용
     */
    @Transactional
    public void useRaffle(Long userId, Long eventId) {
        String lockKey = "lock:raffle:use:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(5, 2, TimeUnit.SECONDS);
            if (!available) {
                throw new IllegalStateException("잠시 후 다시 시도해 주세요.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

            // 1. 응모권 소모
            user.useRaffle();
            userRepository.save(user);

            // 2. 응모 내역 생성 (어떤 이벤트에 응모했는지 명시)
            raffleRepository.save(Raffle.builder()
                    .user(user)
                    .event(event)
                    .raffleDate(LocalDate.now())
                    .build());

            log.info("User {} entered event [{}]. Remaining tickets: {}", 
                userId, event.getTitle(), user.getRaffleCount());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("시스템 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
