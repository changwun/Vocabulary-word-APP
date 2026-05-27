package com.example.quiz.controller;

import com.example.quiz.entity.Event;
import com.example.quiz.security.JwtProvider;
import com.example.quiz.service.EventService;
import com.example.quiz.service.RaffleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "이벤트(Event)", description = "진행 중인 이벤트 조회 및 응모를 담당하는 API입니다.")
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RaffleService raffleService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "진행 중인 이벤트 목록 조회", description = "현재 응모 가능한 모든 이벤트 리스트를 가져옵니다.")
    @GetMapping("/active")
    public ResponseEntity<List<Event>> getActiveEvents() {
        return ResponseEntity.ok(eventService.getActiveEvents());
    }

    @Operation(summary = "이벤트에 응모권 사용", description = "특정 이벤트 ID를 지정하여 보유한 응모권 1개를 소모합니다.")
    @PostMapping("/{eventId}/enter")
    public ResponseEntity<String> enterEvent(
            @Parameter(description = "Bearer {token}") @RequestHeader("Authorization") String authHeader,
            @PathVariable Long eventId) {
        Long userId = authenticate(authHeader);
        raffleService.useRaffle(userId, eventId);
        return ResponseEntity.ok("이벤트 응모가 완료되었습니다!");
    }

    private Long authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 인증 헤더입니다.");
        }
        return jwtProvider.validateAndGetUserId(authHeader.substring(7));
    }
}
