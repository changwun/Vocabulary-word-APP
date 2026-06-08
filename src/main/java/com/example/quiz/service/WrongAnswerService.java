package com.example.quiz.service;

import com.example.quiz.dto.QuizDto;
import com.example.quiz.entity.User;
import com.example.quiz.repository.WrongAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongAnswerService {

    private final WrongAnswerRepository wrongAnswerRepository;

    @Transactional(readOnly = true)
    public List<QuizDto.WrongAnswerResponse> getWrongAnswers(Long userId) {
        return wrongAnswerRepository.findAllByUserIdWithWord(userId).stream()
                .map(wa -> QuizDto.WrongAnswerResponse.builder()
                        .id(wa.getId())
                        .english(wa.getWord().getEnglish())
                        .korean(wa.getWord().getKorean())
                        .wrongCount(wa.getWrongCount())
                        .lastAttemptAt(wa.getLastAttemptAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteWrongAnswer(Long userId, Long id) {
        wrongAnswerRepository.findById(id).ifPresent(wa -> {
            if (wa.getUser().getId().equals(userId)) {
                wrongAnswerRepository.delete(wa);
            } else {
                throw new IllegalStateException("해당 오답 기록에 대한 삭제 권한이 없습니다.");
            }
        });
    }
}
