package com.example.quiz.controller;

import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.RaffleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "응모(Raffle)", description = "응모권 사용 및 추첨 관리를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/raffle")
@RequiredArgsConstructor
public class RaffleController {

    private final RaffleService raffleService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "응모권 사용하기", description = "보유한 응모권 1개를 소모하여 경품 추첨에 응모합니다. 분산 락이 적용되어 안전합니다.")
    @PostMapping("/use")
    public ResponseEntity<String> useRaffle(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader) {
        Long userId = authenticate(authHeader);
        raffleService.useRaffle(userId);
        return ResponseEntity.ok("응모가 완료되었습니다!");
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
