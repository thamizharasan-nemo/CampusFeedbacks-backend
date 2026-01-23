package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginReqDTO {
    String email;
    String password;
}
