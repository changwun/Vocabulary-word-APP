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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자(Admin)", description = "서비스 관리 및 통계 조회를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RaffleService raffleService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Operation(summary = "대시보드 통계 조회", description = "전체 유저 수, 응모 횟수, 이벤트별 참여자 수 등을 조회합니다. 관리자 전용입니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<QuizDto.AdminDashboardResponse> getDashboard(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isAdmin()) {
            throw new IllegalStateException("관리자 권한이 없습니다.");
        }

        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @Operation(summary = "이벤트 당첨자 추첨", description = "특정 이벤트의 응모자 중 당첨자를 랜덤 선발합니다. 관리자 전용입니다.")
    @PostMapping("/event/{eventId}/draw")
    public ResponseEntity<List<String>> drawWinners(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "10") int count) {
        
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isAdmin()) {
            throw new IllegalStateException("관리자 권한이 없습니다.");
        }

        return ResponseEntity.ok(raffleService.drawWinners(eventId, count));
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
