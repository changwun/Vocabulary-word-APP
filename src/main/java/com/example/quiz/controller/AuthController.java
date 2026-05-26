package com.example.quiz.controller;

import com.example.quiz.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    @Getter @Setter
    static class SignUpRequest {
        private String username;
        private String email;
        private String password;
    }

    @Getter @Setter
    static class LoginRequest {
        private String email;
        private String password;
    }
}
