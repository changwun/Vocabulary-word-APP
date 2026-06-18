package com.example.quiz.service;

import com.example.quiz.entity.AuthProvider;
import com.example.quiz.entity.User;
import com.example.quiz.entity.UserRole;
import com.example.quiz.repository.UserRepository;
import com.example.quiz.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.User;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    // 실제로는 Spring Security의 BCryptPasswordEncoder를 주입받아 사용해야 함
    // private final PasswordEncoder passwordEncoder;


    @Transactional
    public void signUp(String username, String email, String phoneNumber, String password, 
                       QuizDto.QuizMode quizMode, boolean privacyPolicyAgreed, java.time.LocalTime notificationTime) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
        
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalStateException("이미 사용 중인 전화번호입니다.");
        }

        if (!privacyPolicyAgreed) {
            throw new IllegalArgumentException("개인정보 수집 및 이용에 동의해야 합니다.");
        }

        // 실무 암호화 시뮬레이션 (BCrypt 등을 사용해야 함)
        String encodedPassword = encodePassword(password);

        User user = User.builder()
                .username(username)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(encodedPassword)
                .role(UserRole.ROLE_USER)
                .quizMode(quizMode)
                .privacyPolicyAgreed(privacyPolicyAgreed)
                .notificationTime(notificationTime)
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

    @Transactional
    public String socialLogin(AuthProvider provider, String providerId, String email, String username) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 신규 유저 생성 (소셜 전용)
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .phoneNumber("소셜가입") // 소셜 가입 시 임시 처리, 추후 정보 수정 유도
                            .provider(provider)
                            .providerId(providerId)
                            .role(UserRole.ROLE_USER)
                            .privacyPolicyAgreed(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // 기존 이메일 유저인 경우 소셜 연동 처리
        if (user.getProvider() == AuthProvider.LOCAL) {
            user.linkSocialAccount(provider, providerId);
            userRepository.save(user);
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
