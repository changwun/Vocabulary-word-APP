package com.example.quiz.service;

import com.example.quiz.entity.Raffle;
import com.example.quiz.entity.User;
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
    private final RedissonClient redissonClient;

    /**
     * 분산 락을 활용한 응모권 사용 로직
     * '따닥' 클릭(동시성 이슈)을 원천 차단합니다.
     */
    @Transactional
    public void useRaffle(Long userId) {
        String lockKey = "lock:raffle:use:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 1. 락 획득 시도 (최대 5초 대기, 2초간 유지)
            boolean available = lock.tryLock(5, 2, TimeUnit.SECONDS);

            if (!available) {
                throw new IllegalStateException("잠시 후 다시 시도해 주세요. (요청이 너무 많습니다)");
            }

            // 2. 비즈니스 로직 수행
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 보유 응모권 차감 (내부적으로 0개 이하인지 체크함)
            user.useRaffle();
            userRepository.save(user);

            // 응모 내역 기록
            raffleRepository.save(Raffle.builder()
                    .user(user)
                    .raffleDate(LocalDate.now())
                    .build());

            log.info("User {} used a raffle. Remaining: {}", userId, user.getRaffleCount());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("시스템 오류가 발생했습니다.");
        } finally {
            // 3. 락 해제 (현재 스레드가 락을 가지고 있는 경우에만)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
