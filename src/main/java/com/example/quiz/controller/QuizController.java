package com.example.quiz.controller;

import com.example.quiz.entity.Word;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    @Operation(summary = "오늘의 단어 조회", description = "사용자에게 할당된 오늘의 영단어 5개를 조회합니다.")
    @GetMapping("/daily")
    public ResponseEntity<List<Word>> getDailyWords(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        List<Word> words = quizService.getDailyWords(userId);
        return ResponseEntity.ok(words);
    }

    @Operation(summary = "퀴즈 완료 및 응모", description = "퀴즈를 완료하고 응모권을 발급받습니다.")
    @PostMapping("/complete")
    public ResponseEntity<String> completeQuiz(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizCompleteRequest request) {
        Long userId = authenticate(authHeader);
        // 최소한의 보안: 요청한 단어 목록이 오늘 할당된 것과 일치하는지 서비스에서 검증
        quizService.completeQuiz(userId, request.getWordIds());
        return ResponseEntity.ok("응모권이 성공적으로 발급되었습니다.");
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }

    @Getter @Setter
    public static class QuizCompleteRequest {
        private List<Long> wordIds;
    }
}
