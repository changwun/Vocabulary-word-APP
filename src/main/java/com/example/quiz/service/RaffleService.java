package com.example.quiz.service;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.Event;
import com.example.quiz.entity.Raffle;
import com.example.quiz.entity.User;
import com.example.quiz.entity.Winner;
import com.example.quiz.repository.EventRepository;
import com.example.quiz.repository.RaffleRepository;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.repository.WinnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaffleService {

    private final UserRepository userRepository;
    private final RaffleRepository raffleRepository;
    private final EventRepository eventRepository;
    private final WinnerRepository winnerRepository;
    private final RedissonClient redissonClient;

    /**
     * 응모권 사용 로직
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

            if (!event.isActive() || event.isDrawn()) {
                throw new IllegalStateException("이미 마감된 이벤트입니다.");
            }

            user.useRaffle();
            userRepository.save(user);

            raffleRepository.save(Raffle.builder()
                    .user(user)
                    .event(event)
                    .raffleDate(LocalDate.now())
                    .build());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("시스템 오류");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * [자동화] 매일 자정(00:00) 마감된 이벤트 자동 추첨
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoDrawWinners() {
        log.info("자정 자동 추첨 스케줄러 가동 중...");
        LocalDateTime now = LocalDateTime.now();
        List<Event> pendingEvents = eventRepository.findAllByActiveTrueAndEndDateBefore(now);
        
        for (Event event : pendingEvents) {
            try {
                int count = event.getTitle().contains("커피") ? 10 : 1;
                drawWinners(event.getId(), count);
            } catch (Exception e) {
                log.error("자동 추첨 실패 [{}]: {}", event.getTitle(), e.getMessage());
            }
        }
    }

    /**
     * 추첨 실행 로직 (예외 처리 강화)
     */
    @Transactional
    public List<String> drawWinners(Long eventId, int winnerCount) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이벤트입니다."));

        if (event.isDrawn()) {
            throw new IllegalStateException("이미 추첨이 완료된 이벤트입니다.");
        }

        List<Raffle> allEntries = raffleRepository.findAllByEvent(event);
        
        // 1. 응모자가 0명인 경우 처리
        if (allEntries.isEmpty()) {
            event.markAsDrawn(); // 추첨은 시도했으나 0명으로 종료
            eventRepository.save(event);
            log.warn("이벤트 [{}]에 응모자가 없어 추첨 없이 종료되었습니다.", event.getTitle());
            throw new IllegalStateException("응모자가 없어 추첨을 진행할 수 없습니다.");
        }

        Collections.shuffle(allEntries);

        List<User> winners = allEntries.stream()
                .map(Raffle::getUser)
                .distinct()
                .limit(winnerCount)
                .collect(Collectors.toList());

        for (User winnerUser : winners) {
            winnerRepository.save(Winner.builder()
                    .user(winnerUser)
                    .event(event)
                    .build());
        }

        event.markAsDrawn();
        eventRepository.save(event);

        return winners.stream()
                .map(u -> maskName(u.getUsername()) + " (" + maskEmail(u.getEmail()) + ")")
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 당첨 공지 조회
     */
    @Transactional(readOnly = true)
    public List<QuizDto.WinnerAnnouncement> getWinnersByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        
        return winnerRepository.findAllByWonAtBetween(start, end).stream()
                .map(w -> QuizDto.WinnerAnnouncement.builder()
                        .maskedUsername(maskName(w.getUser().getUsername()))
                        .maskedEmail(maskEmail(w.getUser().getEmail()))
                        .wonAt(w.getWonAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizDto.WinnerAnnouncement> getWinnersAnnouncement(Long eventId) {
        return winnerRepository.findAllByEventId(eventId).stream()
                .map(w -> QuizDto.WinnerAnnouncement.builder()
                        .maskedUsername(maskName(w.getUser().getUsername()))
                        .maskedEmail(maskEmail(w.getUser().getEmail()))
                        .wonAt(w.getWonAt())
                        .build())
                .collect(Collectors.toList());
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return "*";
        return name.charAt(0) + "*" + name.substring(name.length() - 1);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        String id = parts[0];
        if (id.length() <= 3) return id.charAt(0) + "***@" + parts[1];
        return id.substring(0, 3) + "****@" + parts[1];
    }
}
