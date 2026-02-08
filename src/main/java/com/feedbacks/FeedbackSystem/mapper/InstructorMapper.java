package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    private final PasswordEncoder passwordEncoder;

    public InstructorMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toUser(InstructorRequestDTO dto,
                       User user,
                       Institution institution,
                       boolean isCreate) {

        user.setUsername(dto.getInstructorName());
        user.setEmail(dto.getEmail());
        user.setIdentityNo(dto.getIdentityNo());
        user.setInstitution(institution);

        if (isCreate) {
            user.setRole(User.Role.TEACHER);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return user;
    }

    public Instructor toEntity(User user){
        Instructor instructor = new Instructor();
        instructor.setUser(user);
        return instructor;
    }

    public InstructorResponseDTO toResponse(Instructor instructor){
        InstructorResponseDTO responseDTO = new InstructorResponseDTO();
        responseDTO.setInstructorId(instructor.getInstructorId());
        responseDTO.setInstructorName(instructor.getUser().getUsername());
        responseDTO.setEmail(instructor.getUser().getEmail());
        responseDTO.setIdentityNo(instructor.getUser().getIdentityNo());
        responseDTO.setAssignedCourses(
                instructor.getCourses().stream()
                        .map(course -> new CourseResponseDTO(
                                course.getCourseId(),
                                course.getCourseName(),
                                course.getCourseDescription(),
                                instructor.getUser().getUsername(),
                                course.getAvgRating(),
                                course.getFeedbackCount(),
                                course.getDeletedAt() != null ? course.getDeletedAt().toString() : "Not yet deleted",
                                course.getDeletedBy(),
                                course.getRestoredBy(),
                                course.getInstitution().getInstitutionId())
                        ).toList()
        );
        responseDTO.setAvgRating(instructor.getAvgRating());
        responseDTO.setFeedbackCount(instructor.getFeedbackCount());
        responseDTO.setDeletedAt(instructor.getDeletedAt() != null ? instructor.getDeletedAt().toString() : "Not yet deleted");
        responseDTO.setDeletedBy(instructor.getDeletedBy() != null ? instructor.getDeletedBy() : "No one");
        responseDTO.setRestoredBy(instructor.getRestoredBy() != null ? instructor.getRestoredBy() : "No one");
        return responseDTO;
    }
}

