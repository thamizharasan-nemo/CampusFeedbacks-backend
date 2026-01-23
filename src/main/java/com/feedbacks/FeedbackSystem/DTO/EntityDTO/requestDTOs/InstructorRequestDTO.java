package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstructorRequestDTO {

    @NotBlank(message = "Instructor name is required")
    private String instructorName;

    @NotBlank(message = "Instructor ID is required")
    @Size(min = 6, max = 15)
    private String identityNo;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, max = 20)
    private String password;

    private String description;
}

