package com.feedbacks.FeedbackSystem.DTO.analytics;

public record CourseRankingDTO(
        Integer courseId,
        String courseName,
        String instructorName,
        Double avgRating,
        Long feedbackCount
){}
