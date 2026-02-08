package com.feedbacks.FeedbackSystem.mapper;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.repository.InstitutionRepository;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    private final InstructorServiceImpl instructorService;
    private final InstitutionRepository institutionRepository;

    public CourseMapper(InstructorServiceImpl instructorService,
                        InstitutionRepository institutionRepository) {
        this.instructorService = instructorService;
        this.institutionRepository = institutionRepository;
    }

    public Course toEntity(@Valid CourseRequestDTO requestDTO) {

        Institution institution = institutionRepository.findById(SecurityUtils.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        Course course = new Course();
        course.setCourseName(requestDTO.getCourseName());
        course.setCourseDescription(requestDTO.getCourseDescription());
        course.setInstitution(institution);

        if (requestDTO.getInstructorId() != null) {
            Instructor instructor =
                    instructorService.getInstructorById(requestDTO.getInstructorId());
            course.setInstructor(instructor);
        }

        return course;
    }

    public Course forGettingExists(Course course, @Valid CourseRequestDTO requestDTO) {

        Institution institution = institutionRepository.findById(SecurityUtils.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found"));

        course.setCourseName(requestDTO.getCourseName());
        course.setCourseDescription(requestDTO.getCourseDescription());
        course.setInstitution(institution);

        if (requestDTO.getInstructorId() != null) {
            Instructor instructor =
                    instructorService.getInstructorById(requestDTO.getInstructorId());
            course.setInstructor(instructor);
        }

        return course;
    }

    public CourseResponseDTO toResponse(Course course) {

        String instructorName = null;
        if (course.getInstructor() != null) {
            instructorName = course.getInstructor().getUser().getUsername();
        }

        return new CourseResponseDTO(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDescription(),
                instructorName,
                course.getAvgRating(),
                course.getFeedbackCount(),
                course.getDeletedAt() != null ? course.getDeletedAt().toString() : "Not yet deleted",
                course.getDeletedBy(),
                course.getRestoredBy(),
                course.getInstitution().getInstitutionId()
        );
    }
}

