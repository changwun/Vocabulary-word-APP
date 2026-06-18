package com.example.quiz.repository;

import com.example.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    
    // 알림용 쿼리
    List<User> findAllByNotificationEnabledTrue();
    List<User> findAllByNotificationEnabledTrueAndNotificationTime(LocalTime time);
}
