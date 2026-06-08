package com.example.quiz.controller;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.WrongAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "오답노트(WrongAnswer)", description = "유저별 틀린 단어 관리 API입니다.")
@RestController
@RequestMapping("/api/wrong-answer")
@RequiredArgsConstructor
public class WrongAnswerController {

    private final WrongAnswerService wrongAnswerService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "오답 목록 조회", description = "내가 틀린 단어 리스트를 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<QuizDto.WrongAnswerResponse>> getList(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        return ResponseEntity.ok(wrongAnswerService.getWrongAnswers(userId));
    }

    @Operation(summary = "오답 삭제 (학습 완료)", description = "특정 오답 기록을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = authenticate(authHeader);
        wrongAnswerService.deleteWrongAnswer(userId, id);
        return ResponseEntity.ok("학습 완료 처리되었습니다.");
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
