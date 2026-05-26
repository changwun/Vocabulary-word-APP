package com.example.quiz.controller;

import com.example.quiz.entity.Word;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.QuizService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final JwtProvider jwtProvider;

    @GetMapping("/daily")
    public ResponseEntity<List<Word>> getDailyWords(@RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        List<Word> words = quizService.getDailyWords(userId);
        return ResponseEntity.ok(words);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeQuiz(
            @RequestHeader("Authorization") String authHeader,
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
