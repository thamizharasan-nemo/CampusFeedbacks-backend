package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstitutionRegistrationRequest {
    String institutionName;
    String institutionCode;
    String institutionEmail;
    String address;
    String adminName;
    String adminEmail;
    String password;
    String identityNo;
}
