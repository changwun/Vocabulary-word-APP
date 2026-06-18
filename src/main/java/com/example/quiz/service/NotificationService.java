package com.example.quiz.service;

import com.example.quiz.entity.User;
import com.example.quiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    
    // QuizService의 키를 그대로 사용하기 위해 복사 또는 공유 전략 사용
    private static final String ATTEMPT_KEY_PREFIX = "quiz:attempt:";

    /**
     * [알림 1] 사용자가 설정한 맞춤 시간 알림 (매 분마다 체크)
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendCustomTimeNotifications() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        log.info("Checking for custom time notifications at {}", now);

        List<User> usersToNotify = userRepository.findAllByNotificationEnabledTrueAndNotificationTime(now);
        
        for (User user : usersToNotify) {
            sendNotification(user, "오늘의 럭키보카 퀴즈가 도착했습니다! 📚", "설정하신 시간이 되었습니다. 지금 바로 퀴즈를 풀고 응모권을 획득하세요!");
        }
    }

    /**
     * [알림 2] 퀴즈 마감 임박 알림 (매일 밤 11시 00분 발송)
     * 자정 마감 1시간 전 기준
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void sendDeadlineReminder() {
        log.info("Checking for deadline reminders...");
        String todayStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        
        List<User> users = userRepository.findAllByNotificationEnabledTrue();
        
        for (User user : users) {
            String attemptKey = ATTEMPT_KEY_PREFIX + todayStr + ":" + user.getId();
            
            // 오늘 아직 퀴즈를 풀지 않은 유저에게만 발송
            if (!redisTemplate.hasKey(attemptKey)) {
                sendNotification(user, "⚠️ 퀴즈 마감 1시간 전!", "오늘의 퀴즈를 아직 완료하지 않으셨네요! 1시간 뒤면 기회가 사라집니다. 서두르세요!");
            }
        }
    }

    /**
     * 실제 알림 발송 로직 (현재는 로그로 시뮬레이션, 추후 JavaMailSender 연동)
     */
    private void sendNotification(User user, String title, String content) {
        log.info("[NOTIFICATION SENT] To: {} ({}), Title: {}, Content: {}", 
            user.getUsername(), user.getEmail(), title, content);
        
        // TODO: JavaMailSender.send(...) 또는 카카오 알림톡 API 호출
    }
}
