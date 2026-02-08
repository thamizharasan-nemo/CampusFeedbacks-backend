package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDTO {
    private int courseId;
    private String courseName;
    private String courseDescription;
    private String instructorName;
    private double avgRating;
    private long feedbackCount;
    private String deletedAt;
    private String deletedBy;
    private String restoredBy;
    private Integer institutionId;
}
