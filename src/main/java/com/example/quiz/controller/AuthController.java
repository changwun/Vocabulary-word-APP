package com.example.quiz.controller;

import com.example.quiz.service.AuthService;
import com.example.quiz.dto.QuizDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증(Auth)", description = "회원가입 및 로그인을 담당하는 API입니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 전화번호, 개인정보 동의 및 퀴즈 모드를 포함합니다.")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(
                request.getUsername(), 
                request.getEmail(), 
                request.getPhoneNumber(),
                request.getPassword(), 
                request.getQuizMode(),
                request.isPrivacyPolicyAgreed());
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    @Getter @Setter
    static class SignUpRequest {
        private String username;
        private String email;
        private String phoneNumber;
        private String password;
        private QuizDto.QuizMode quizMode;
        private boolean privacyPolicyAgreed;
    }

    @Getter @Setter
    static class LoginRequest {
        private String email;
        private String password;
    }
}
