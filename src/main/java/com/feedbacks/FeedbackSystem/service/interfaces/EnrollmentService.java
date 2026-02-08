package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponseDTO getEnrollmentResponseById(int enrollmentId);

    List<EnrollmentResponseDTO> getAllEnrollmentsByStudentId();

    Page<EnrollmentResponseDTO> getAllEnrollmentsToInstitutionAsPage(int page, int size);

    List<CourseResponseDTO> getStudentEnrolledCourse();

    List<EnrollmentResponseDTO> getAllEnrollmentsByRollNo(String rollNo);

    List<EnrollmentResponseDTO> getEnrollmentsToInstitution();

    EnrollmentResponseDTO findEnrollmentByStudentIdAndCourseId(EnrollmentRequestDTO requestDTO);

    Boolean isEnrolled(int courseId);

    EnrollmentResponseDTO enrollToCourse(EnrollmentRequestDTO requestDTO);

    void unrollToCourse(int requestDTO);

    int getCourseEnrollmentCount(int courseId);

    int getStudentEnrollmentCount(int studentId);

    long countEnrollsByInstitutionId();


}
