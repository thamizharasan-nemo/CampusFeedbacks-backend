package com.feedbacks.FeedbackSystem.DTO.analytics;

public record RateLimitInfo(
        Integer limit,
        Integer remaining,
        String resetAt
) {}
