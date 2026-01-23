package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstitutionResponseDTO;
import com.feedbacks.FeedbackSystem.model.Institution;
import org.springframework.stereotype.Component;

@Component
public class InstitutionMapper {

    public Institution toEntity(Institution institution, InstitutionRequestDTO dto){
        institution.setInstitutionName(dto.getInstitutionName());
        institution.setInstitutionCode(dto.getInstitutionCode());
        institution.setEmail(dto.getEmail());
        institution.setAddress(dto.getAddress());
        return institution;
    }

    public InstitutionResponseDTO toResponse(Institution institution){
        return new InstitutionResponseDTO(
                institution.getInstitutionId(),
                institution.getInstitutionName(),
                institution.getInstitutionCode(),
                institution.getEmail(),
                institution.getAddress(),
                String.valueOf(institution.getCreatedAt())
        );
    }
}

