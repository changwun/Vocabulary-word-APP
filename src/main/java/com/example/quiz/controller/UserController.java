package com.example.quiz.controller;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.User;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.AdminService;
import com.example.quiz.service.RaffleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "사용자(User)", description = "사용자 정보 및 설정 관리를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AdminService adminService;
    private final RaffleService raffleService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보(응모권 개수, 퀴즈 모드 등)를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<QuizDto.UserInfoResponse> getMyInfo(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(QuizDto.UserInfoResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .raffleCount(user.getRaffleCount())
                .quizMode(user.getQuizMode())
                .role(user.getRole().name())
                .notificationTime(user.getNotificationTime())
                .notificationEnabled(user.isNotificationEnabled())
                .build());
    }

    @Operation(summary = "내 정보 수정", description = "사용자 이름과 전화번호를 수정합니다.")
    @PutMapping("/me")
    public ResponseEntity<String> updateMyInfo(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizDto.UserUpdateRequest request) {
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 전화번호 중복 체크 (본인 제외)
        if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), userId)) {
            throw new IllegalStateException("이미 다른 사용자가 사용 중인 전화번호입니다.");
        }

        user.updateProfile(request.getUsername(), request.getPhoneNumber());
        userRepository.save(user);
        return ResponseEntity.ok("정보가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "알림 설정 수정", description = "퀴즈 알림 시간 및 수신 여부를 수정합니다.")
    @PutMapping("/notification")
    public ResponseEntity<String> updateNotification(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizDto.NotificationUpdateRequest request) {
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateNotificationSettings(request.getNotificationTime(), request.isNotificationEnabled());
        userRepository.save(user);
        return ResponseEntity.ok("알림 설정이 변경되었습니다.");
    }

    @Operation(summary = "퀴즈 모드 변경", description = "퀴즈 풀이 모드(영->한, 한->영)를 변경합니다.")
    @PutMapping("/mode")
    public ResponseEntity<String> updateQuizMode(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizDto.ModeUpdateRequest request) {
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateQuizMode(request.getQuizMode());
        userRepository.save(user);
        return ResponseEntity.ok("퀴즈 모드가 변경되었습니다.");
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
