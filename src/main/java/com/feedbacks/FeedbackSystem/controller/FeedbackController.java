package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.service.interfaces.FeedbackService;
import com.feedbacks.FeedbackSystem.service.serviceImple.FeedbackServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/institutions")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getAllFeedbacksByInstituteId() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Institution feedbacks fetched",
                feedbackService.getAllFeedbacksByInstituteId()
        ));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> getById(@PathVariable int feedbackId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback fetched successfully",
                feedbackService.getFeedbackResponseById(feedbackId)
        ));
    }

    // Returns all feedback, only for checking
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FeedbackResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt,DESC") String sort) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback list fetched",
                feedbackService.getAllFeedbacks(page, size, sort)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> submitFeedback(@Valid @RequestBody FeedbackRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Feedback submitted successfully",
                        feedbackService.submitFeedback(request)
                ));
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> editFeedback(@PathVariable int feedbackId,
                                                                   @Valid @RequestBody FeedbackRequestDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback updated successfully",
                feedbackService.editFeedback(feedbackId, request)
        ));
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable int feedbackId) {
        feedbackService.deleteFeedbackById(feedbackId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback deleted successfully",
                null
        ));
    }

    @PutMapping("/{feedbackId}/restore")
    public ResponseEntity<ApiResponse<FeedbackResponseDTO>> restore(@PathVariable int feedbackId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback restored successfully",
                feedbackService.restoreFeedback(feedbackId)
        ));
    }

    @DeleteMapping("/{feedbackId}/permanent")
    public ResponseEntity<ApiResponse<Void>> deletePermanently(@PathVariable int feedbackId) {
        feedbackService.deleteFeedbackPermanently(feedbackId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback permanently deleted",
                null
        ));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getSoftDeleted() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Deleted feedbacks fetched",
                feedbackService.findAllSoftDeletedFeedbacks()
        ));
    }


    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<Page<FeedbackResponseDTO>>> getByCourse(
            @PathVariable int courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt,DESC") String sort) {

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course feedback fetched",
                feedbackService.getSortedFeedbackByCourseId(courseId, page, size, sort)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> search(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Boolean anonymous,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Search results fetched",
                feedbackService.searchFeedback(
                        courseId, studentId, minRating,
                        keyword, studentName, anonymous,
                        fromDate, toDate
                )
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> filterFeedback(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Boolean anonymous) {

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Filtered feedback fetched",
                feedbackService.getFilteredFeedback(courseId, minRating, maxRating, fromDate, toDate, anonymous)
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<FeedbackResponseDTO>>> myFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt,DESC") String sort) {

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Your feedbacks fetched",
                feedbackService.getFeedbackByStudent(page, size, sort)
        ));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getFeedbacksByEnrollment(@PathVariable("enrollmentId") int enrollmentId){
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedbacks for this enrollment",
                        feedbackService.getFeedbacksByEnrollment(enrollmentId)
                )
        );
    }

    @GetMapping("/courses/average/rating")
    public ResponseEntity<ApiResponse<?>> getAvgRatingForCourse(@RequestParam Integer courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Average rating fetched",
                feedbackService.getAverageCourseRating(courseId)
                )
        );
    }

    @GetMapping("/instructors/average/rating")
    public ResponseEntity<ApiResponse<?>> getAvgRatingForInstructor(@RequestParam Integer instructorId) {
        return ResponseEntity.ok(new ApiResponse<>(
                        true,
                        "Average rating fetched",
                        feedbackService.getAverageInstructorRating(instructorId)
                )
        );
    }

    @GetMapping("/courses/summary")
    public ResponseEntity<ApiResponse<List<CourseFeedbackSummary>>> getCourseSummaries() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Fetched course summary",
                feedbackService.getCourseSummary())
        );
    }

    @GetMapping("/courses/{courseId}/feedbacks")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getFeedbacksByStudentAndCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedbacks fetched for a student to a course",
                feedbackService.getFeedbacksByStudentAndCourse(courseId))
        );
    }

    @GetMapping("/recent/{courseId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDTO>>> getRecentFeedbacksByCourseId(@PathVariable Integer courseId,
                                                                                  @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Fetched recent feedbacks",
                feedbackService.getRecentFeedbacksByCourseId(courseId, limit))
        );
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> feedbacksCountByInstitution() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Total instructor count fetched for institution",
                feedbackService.countTotalFeedbacksByInstitution()
        ));
    }

}
