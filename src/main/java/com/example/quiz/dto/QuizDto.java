package com.example.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class QuizDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long wordId;
        private String question; // 영어 단어 또는 한글 뜻
        private QuizMode mode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRequest {
        private Long wordId;
        private String answer;
    }
@Getter
@NoArgsConstructor
@AllArgsConstructor
public static class QuizCompleteRequest {
    private List<AnswerRequest> answers;
    private QuizMode mode;
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public static class QuizResultResponse {
    private boolean success;
    private List<WrongAnswerDetail> wrongDetails;
    private String message;
    private int raffleCount;
}

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public static class WrongAnswerDetail {
    private Long wordId;
    private String question;
    private String correctAnswer;
    private String userAnswer;
}

public enum QuizMode {

        EN_TO_KO, // 영어 보고 한글 맞추기
        KO_TO_EN  // 한글 보고 영어 맞추기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoResponse {
        private String username;
        private int raffleCount;
        private QuizMode quizMode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeUpdateRequest {
        private QuizMode quizMode;
    }
}
