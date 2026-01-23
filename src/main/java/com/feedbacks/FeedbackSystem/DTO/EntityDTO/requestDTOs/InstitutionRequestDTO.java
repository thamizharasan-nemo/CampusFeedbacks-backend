package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionRequestDTO {

    @NotBlank
    @Size(min = 3, max = 100)
    private String institutionName;

    @NotBlank
    @Size(min = 3, max = 20)
    private String institutionCode;

    @Email
    @NotBlank
    private String email;

    private String address;
}

