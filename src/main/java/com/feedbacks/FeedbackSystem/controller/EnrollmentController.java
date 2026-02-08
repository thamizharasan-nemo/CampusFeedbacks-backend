package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.EnrollmentRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.EnrollmentResponseDTO;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.service.interfaces.EnrollmentService;
import com.feedbacks.FeedbackSystem.service.serviceImple.EnrollmentServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> getEnrollmentById(@PathVariable int enrollmentId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Enrollment retrieved successfully to id " + enrollmentId,
                        enrollmentService.getEnrollmentResponseById(enrollmentId)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/institution/all")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponseDTO>>> getAllEnrollmentByInstituteId(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Enrollments retrieved successfully",
                        enrollmentService.getAllEnrollmentsToInstitutionAsPage(page, size)
                )
        );
    }

    @GetMapping("/{courseId}/enrollment-status")
    public ResponseEntity<ApiResponse<Boolean>> isStudentEnrolledToCourse(@PathVariable("courseId") int courseId){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student has enrolled",
                        enrollmentService.isEnrolled(courseId)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> enrollToCourse(@RequestBody EnrollmentRequestDTO requestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student enrolled successfully",
                        enrollmentService.enrollToCourse(requestDTO)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> unrollFromCourse(@PathVariable int courseId) {
        enrollmentService.unrollToCourse(courseId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student unrolled successfully",
                        "Enrollment removed"
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByStudentId() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student enrollments retrieved",
                        enrollmentService.getAllEnrollmentsByStudentId()
                )
        );
    }

    @GetMapping("/courses/student/enrolled")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getStudentEnrolledCourse() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Student enrolled courses fetched",
                        enrollmentService.getStudentEnrolledCourse())
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/rollno")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByRollNo(@RequestParam String rollNo) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Enrollments retrieved by roll number",
                        enrollmentService.getAllEnrollmentsByRollNo(rollNo)
                )
        );
    }

    @GetMapping("/institution")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByInstitutionId() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All enrollments in this institution",
                        enrollmentService.getEnrollmentsToInstitution()
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> findEnrollmentByStudentAndCourse(@RequestBody EnrollmentRequestDTO requestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Enrollment found",
                        enrollmentService.findEnrollmentByStudentIdAndCourseId(requestDTO)
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCourseEnrollmentCount(@PathVariable int courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Course enrollment count retrieved",
                        enrollmentService.getCourseEnrollmentCount(courseId)
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<ApiResponse<Integer>> getStudentEnrollmentCount(@PathVariable int studentId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Student enrollment count retrieved",
                        enrollmentService.getStudentEnrollmentCount(studentId)
                )
        );
    }

    @GetMapping("/institution/count")
    public ResponseEntity<ApiResponse<Long>> getEnrollmentCount(){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Count of enrollments of this institution",
                        enrollmentService.countEnrollsByInstitutionId()
                )
        );
    }
}
