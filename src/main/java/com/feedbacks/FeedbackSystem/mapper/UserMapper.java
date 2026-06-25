package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.InstitutionRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final InstitutionRepository institutionRepo;

    public UserMapper(PasswordEncoder passwordEncoder,
                      InstitutionRepository institutionRepo) {
        this.passwordEncoder = passwordEncoder;
        this.institutionRepo = institutionRepo;
    }

    public User toEntity(String institutionCode, User user, @Valid UserRequestDTO dto){

        Institution institution = institutionRepo.findByInstitutionCode(institutionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setIdentityNo(dto.getIdentityNo());
        user.setUserCreatedAt(LocalDate.now());
        user.setInstitution(institution);

        return user;
    }

    public UserResponseDTO toResponse(User user){
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getIdentityNo(),
                user.getRole().toString(),
                String.valueOf(user.getUserCreatedAt()),
                user.getInstitution().getInstitutionId()
        );
    }
}

