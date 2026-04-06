package com.skillbridge.skillbridge.dto;

import com.skillbridge.skillbridge.model.ContentFeedback;

public record UserFeedbackView(
        Long id,
        Long contentId,
        String contentTitle,
        Integer rating,
        String comment,
        String createdAt
) {
    public static UserFeedbackView from(ContentFeedback feedback) {
        return new UserFeedbackView(
                feedback.getId(),
                feedback.getContent().getId(),
                feedback.getContent().getTitle(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt().toString()
        );
    }
}
