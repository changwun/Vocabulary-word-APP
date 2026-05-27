package com.example.quiz.controller;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "퀴즈(Quiz)", description = "영단어 퀴즈 조회 및 응모 완료를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "오늘의 퀴즈 조회", description = "사용자에게 할당된 오늘의 퀴즈 5개를 조회합니다. 사용자가 설정한 기본 퀴즈 모드로 제공됩니다.")
    @GetMapping("/daily")
    public ResponseEntity<List<QuizDto.QuestionResponse>> getDailyQuizzes(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        List<QuizDto.QuestionResponse> quizzes = quizService.getDailyQuizzes(userId);
        return ResponseEntity.ok(quizzes);
    }

    @Operation(summary = "퀴즈 완료 및 응모권 획득", description = "퀴즈 정답을 제출하고 결과를 확인합니다. 5문제를 모두 맞춰야 응모권이 지급됩니다.")
    @PostMapping("/complete")
    public ResponseEntity<QuizDto.QuizResultResponse> completeQuiz(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizDto.QuizCompleteRequest request) {
        Long userId = authenticate(authHeader);
        QuizDto.QuizResultResponse result = quizService.completeQuiz(userId, request);
        return ResponseEntity.ok(result);
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
