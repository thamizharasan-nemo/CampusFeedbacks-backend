package com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionResponseDTO {

    private Integer institutionId;
    private String institutionName;
    private String institutionCode;
    private String email;
    private String address;
    private String createdAt;
}
