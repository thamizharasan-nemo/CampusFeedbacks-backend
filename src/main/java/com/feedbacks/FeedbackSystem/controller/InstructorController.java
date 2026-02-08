package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstructorRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstructorResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instructors")
@Slf4j
public class InstructorController {

    private final InstructorServiceImpl instructorService;

    public InstructorController(InstructorServiceImpl instructorService) {
        this.instructorService = instructorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstructorResponseDTO>>> getAllInstructors() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructors fetched successfully",
                        instructorService.getAllInstructorsAsResponse())
        );
    }

    @GetMapping("/admin/{instructorId}")
    public ResponseEntity<ApiResponse<Instructor>> getInstructorById(@PathVariable int instructorId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor fetched successfully",
                        instructorService.getInstructorById(instructorId))
        );
    }

    @GetMapping("/{instructorId}")
    public ResponseEntity<ApiResponse<InstructorResponseDTO>> getInstructorResponseById(@PathVariable Integer instructorId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor fetched successfully",
                        instructorService.getInstructorResponseById(instructorId))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<InstructorResponseDTO>> addInstructor(@RequestBody InstructorRequestDTO requestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor added successfully",
                        instructorService.addInstructor(requestDTO))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{instructorId}")
    public ResponseEntity<ApiResponse<InstructorResponseDTO>> updateInstructorById(@PathVariable Integer instructorId,
                                                                                   @RequestBody InstructorRequestDTO requestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor updated successfully",
                        instructorService.updateInstructorById(instructorId, requestDTO))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{instructorId}")
    public ResponseEntity<ApiResponse<Void>> deleteInstructorById(@PathVariable Integer instructorId) {
        instructorService.deleteInstructorById(instructorId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Instructor with ID " + instructorId + " deleted successfully",
                        null)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{instructorId}/deleted")
    public ResponseEntity<ApiResponse<Void>> deleteInstructorPermanentlyById(@PathVariable int instructorId,
                                                                             @RequestParam(defaultValue = "true") boolean permanent) {
        instructorService.deleteInstructorPermanently(instructorId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Instructor permanently deleted",
                        null)
        );
    }

    @GetMapping("/soft/deleted")
    public ResponseEntity<ApiResponse<List<InstructorResponseDTO>>> getAllSoftDeletedInstructors() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Soft deleted instructors fetched",
                        instructorService.findAllSoftDeletedInstructors())
        );
    }

    @PutMapping("/{instructorId}/restore")
    public ResponseEntity<ApiResponse<InstructorResponseDTO>> restoreInstructor(
            @PathVariable int instructorId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor restored successfully",
                        instructorService.restoreInstructor(instructorId))
        );
    }

    @PutMapping("/course/{courseId}/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<InstructorResponseDTO>> assignCourse(
            @PathVariable Integer instructorId,
            @PathVariable Integer courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course assigned to instructor",
                        instructorService.assignCourseToInstructor(instructorId, courseId))
        );
    }

    @DeleteMapping("/courses/{courseId}/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<Void>> unassignCourseFromInstructor(@PathVariable int instructorId,
                                                                          @PathVariable int courseId) {

        instructorService.unassignCourseFromInstructor(instructorId, courseId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course unassigned from instructor", null)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/institution/{instructorId}")
    public ResponseEntity<ApiResponse<Void>> unassignInstructorFromInstitution(@PathVariable int instructorId) {
        instructorService.unassignInstructorFromInstitution(instructorId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor unassigned from institution", null)
        );
    }

    @GetMapping("/unassigned/courses")
    public ResponseEntity<ApiResponse<List<InstructorResponseDTO>>> getAllUnassignedInstructors() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Unassigned instructors fetched",
                        instructorService.getUnassignedInstructors())
        );
    }

    @GetMapping("/assigned/courses/{instructorId}")
    public ResponseEntity<ApiResponse<List<String>>> viewAssignedCourses(@PathVariable int instructorId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Assigned courses fetched",
                        instructorService.viewAssignedCourseForInstructor(instructorId))
        );
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbacksByInstructor>>> getAllFeedbacksByInstructor() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor feedbacks fetched",
                        instructorService.getAllFeedbacksByInstructor())
        );
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<TopRatedInstructorsDTO>>> getAllTopRatedInstructors() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Top rated instructors fetched",
                        instructorService.getAllTopRatedInstructors())
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InstructorResponseDTO>>> searchInstructor(
            @RequestParam(required = false) Integer instructorId,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) String courseName) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Search results fetched",
                        instructorService.searchInstructor(instructorId, instructorName, courseName))
        );
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countInstructorsToInstitution() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Total instructor instructors count fetched",
                        instructorService.getTotalInstructorCountByInstitution())
        );
    }
}


