package com.example.quiz.controller;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.User;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자(User)", description = "사용자 정보 및 설정 관리를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보(응모권 개수, 퀴즈 모드 등)를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<QuizDto.UserInfoResponse> getMyInfo(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(QuizDto.UserInfoResponse.builder()
                .username(user.getUsername())
                .raffleCount(user.getRaffleCount())
                .quizMode(user.getQuizMode())
                .build());
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
