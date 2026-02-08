package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentResponseDTO {
    private int enrollmentId;
    private Integer institutionId;
    private String institutionName;
    private String studentName;
    private String studentRollNo;
    @NotNull
    private int courseId;
    @NotNull
    private int instructorId;
    private String courseName;
    private String instructorName;
    private LocalDate enrolledDate;
}

