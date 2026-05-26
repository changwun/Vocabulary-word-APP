package com.example.quiz.service;

import com.example.quiz.entity.User;
import com.example.quiz.entity.UserRole;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 실제로는 Spring Security의 BCryptPasswordEncoder를 주입받아 사용해야 함
    // private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 실무 암호화 시뮬레이션 (BCrypt 등을 사용해야 함)
        String encodedPassword = encodePassword(password);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 저장된 암호화된 비밀번호와 입력받은 비밀번호를 비교 (match 로직 시뮬레이션)
        if (!matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.createToken(user.getId(), user.getRole().name());
    }

    private String encodePassword(String password) {
        // 실제로는 BCrypt.hashpw(password, BCrypt.gensalt())
        return "{bcrypt}" + password; 
    }

    private boolean matches(String rawPassword, String encodedPassword) {
        // 실제로는 passwordEncoder.matches(rawPassword, encodedPassword)
        return encodePassword(rawPassword).equals(encodedPassword);
    }
}
