package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequestDTO {

    @NotNull
    @Min(1)
    @Max(5)
    private int courseRating;
    private String courseComment;

    @NotNull
    @Min(1)
    @Max(5)
    private int instructorRating;
    private String instructorComment;

    private boolean anonymous;

    @NotNull(message = "Course ID is required")
    private int courseId;

    @NotNull(message = "Instructor ID is required")
    private int instructorId;
}


