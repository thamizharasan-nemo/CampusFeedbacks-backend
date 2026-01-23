package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.model.*;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FeedbackMapper {

    public Feedback toEntity(
            FeedbackRequestDTO dto,
            Feedback feedback,
            User student,
            Course course,
            Instructor instructor,
            Institution institution
    ) {

        feedback.setCourseRating(dto.getCourseRating());
        feedback.setInstructorRating(dto.getInstructorRating());
        feedback.setCourseComment(dto.getCourseComment());
        feedback.setInstructorComment(dto.getInstructorComment());
        feedback.setAnonymous(dto.isAnonymous());

        feedback.setStudent(student);
        feedback.setCourse(course);
        feedback.setInstructor(instructor);
        feedback.setInstitution(institution);

        return feedback;
    }

    public FeedbackResponseDTO toResponse(Feedback feedback) {
        return new FeedbackResponseDTO(
                feedback.getFeedbackId(),
                feedback.getCourseRating(),
                feedback.getCourse().getCourseName(),
                feedback.isAnonymous(),
                feedback.isAnonymous()
                        ? "Anonymous"
                        : feedback.getStudent().getUsername(),
                feedback.getInstructorRating(),
                feedback.getCourseComment(),
                feedback.getInstructor().getUser().getUsername(),
                feedback.getSubmittedAt().toLocalDate(),
                feedback.getInstructorComment(),
                feedback.getDeletedAt() != null
                        ? feedback.getDeletedAt().toString()
                        : "Not yet deleted",
                feedback.getDeletedBy(),
                feedback.getRestoredBy()
        );
    }
}
