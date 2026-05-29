package com.example.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long wordId;
        private String question;
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoResponse {
        private String username;
        private int raffleCount;
        private QuizMode quizMode;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminDashboardResponse {
        private long totalUsers;
        private long totalRafflesUsed;
        private long activeEventsCount;
        private List<EventWinnerStat> eventStats;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventWinnerStat {
        private Long eventId;
        private String eventTitle;
        private long participantCount;
        private boolean isDrawn;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WinnerAnnouncement {
        private String maskedUsername;
        private String maskedEmail;
        private LocalDateTime wonAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminWinnerDetail {
        private String username;
        private String email;
        private String phoneNumber;
        private LocalDateTime wonAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeUpdateRequest {
        private QuizMode quizMode;
    }

    public enum QuizMode {
        EN_TO_KO,
        KO_TO_EN
    }
}
