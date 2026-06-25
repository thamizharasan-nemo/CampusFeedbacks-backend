package com.feedbacks.FeedbackSystem.DTO.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopularCourseDTO {
    private int courseId;
    private String courseName;
    private long feedbackCount;
    private String instructorName;
    private double avgRating;
}
