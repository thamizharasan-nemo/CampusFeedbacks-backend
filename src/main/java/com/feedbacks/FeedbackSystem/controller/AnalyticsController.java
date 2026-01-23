package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.FeedbackServiceImpl;
import com.feedbacks.FeedbackSystem.service.serviceImple.InstructorServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final FeedbackServiceImpl feedbackService;
    private final CourseServiceImpl courseService;
    private final InstructorServiceImpl instructorService;

    public AnalyticsController(FeedbackServiceImpl feedbackService, CourseServiceImpl courseService, InstructorServiceImpl instructorService) {
        this.feedbackService = feedbackService;
        this.courseService = courseService;
        this.instructorService = instructorService;
    }

    @GetMapping("/feedbacks/trends")
    public ResponseEntity<ApiResponse<List<FeedbackTrendDTO>>> feedbackTrends(@RequestParam String groupBy) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback trends fetched",
                feedbackService.getFeedbackTrends(groupBy)
        ));
    }

    @GetMapping("/feedbacks/ratings")
    public ResponseEntity<ApiResponse<List<RatingDistributionDTO>>> feedbackRatings() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Feedback rating distribution fetched",
                feedbackService.getFeedbackRatings()
        ));
    }

    @GetMapping("/courses/top")
    public ResponseEntity<ApiResponse<List<CourseRankingDTO>>> courseRanking(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Top rated courses fetched",
                courseService.getCourseRanking(page, size)
        ));
    }

    @GetMapping("/instructors/top")
    public ResponseEntity<ApiResponse<List<InstructorRankingDTO>>> getTopRatedInstructor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Top rated instructors fetched",
                instructorService.getTopRatedInstructor(page, size)
        ));
    }

    @GetMapping("/instructors/rating/last-7-days")
    public ResponseEntity<ApiResponse<Double>> avgRatingOfInstructorsLast7days() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Average instructor rating (last 7 days) fetched",
                feedbackService.avgRatingOfInstructorsLast7days()
        ));
    }

    @GetMapping("/courses/rating/last-7-days")
    public ResponseEntity<ApiResponse<Double>> avgRatingOfCoursesLast7days() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Average course rating (last 7 days) fetched",
                feedbackService.avgRatingOfCoursesLast7days()
        ));
    }

}
