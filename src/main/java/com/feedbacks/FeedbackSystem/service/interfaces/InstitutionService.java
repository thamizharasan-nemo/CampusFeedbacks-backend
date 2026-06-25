package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRegistrationRequest;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstitutionResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;

import java.util.List;

public interface InstitutionService {

    UserResponseDTO registerInstitution(InstitutionRegistrationRequest registerRequest);

    InstitutionResponseDTO createInstitution(InstitutionRequestDTO dto);

    InstitutionResponseDTO getInstitutionById();

    List<InstitutionResponseDTO> getAllInstitutions();

    InstitutionResponseDTO updateInstitution(InstitutionRequestDTO dto);

    void deleteInstitution();

    List<InstitutionResponseDTO> searchInstitutions(String name);

    Long totalUsers();

    Long totalCourses();
}

