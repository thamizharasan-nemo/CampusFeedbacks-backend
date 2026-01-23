package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.CourseRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackCountDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.CourseResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.PopularCourseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.security.SecurityUtils;
import com.feedbacks.FeedbackSystem.service.serviceImple.CourseServiceImpl;
import com.feedbacks.FeedbackSystem.service.other_services.HtmlEmailBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseServiceImpl courseService;
    private final HtmlEmailBody emailBody;

    public CourseController(CourseServiceImpl courseService, HtmlEmailBody emailBody) {
        this.courseService = courseService;
        this.emailBody = emailBody;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "All courses fetched successfully",
                        courseService.getAllCourses())
        );
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllCoursesDTO() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses fetched successfully",
                        courseService.getAllCoursesDTO())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @GetMapping("/id/{courseId}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable int courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course fetched successfully",
                        courseService.getCourseById(courseId))
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseResponseById(@PathVariable int courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course retrieved successfully",
                        courseService.getCourseResponseById(courseId))
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponseDTO>> addCourse(@RequestBody CourseRequestDTO requestDTO) {
        emailBody.newCourseAddedHtmlBody(requestDTO.getCourseName());
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course added successfully",
                        courseService.addCourse(requestDTO))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourseById(@PathVariable int courseId,
                                                                           @RequestBody CourseRequestDTO requestDTO) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course updated successfully",
                        courseService.updateCourseById(courseId, requestDTO))
        );
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourseById(@PathVariable int courseId) {
        courseService.deleteCourseById(courseId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course soft deleted successfully", null)
        );
    }

    @DeleteMapping("/{courseId}/deleted")
    public ResponseEntity<ApiResponse<Void>> deleteCoursePermanentlyById(
            @PathVariable int courseId,
            @RequestParam(defaultValue = "true") boolean permanent) {

        courseService.deleteCoursePermanently(courseId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course permanently deleted", null)
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/institution")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesByInstitution() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Institution courses fetched",
                        courseService.getCoursesByInstitution())
        );
    }

    @GetMapping("/soft/deleted")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllDeletedCourses() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Soft deleted courses fetched",
                        courseService.findAllSoftDeletedCourses())
        );
    }

    @PutMapping("/{courseId}/restore")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> restoreCourse(@PathVariable int courseId) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Course restored successfully",
                        courseService.restoreCourse(courseId))
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> assignCourse(
            @PathVariable int courseId,
            @PathVariable int instructorId) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor assigned to course",
                        courseService.assignInstructorToCourse(courseId, instructorId))
        );
    }

    @PutMapping("/{courseId}/instructor")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> unassignCourse(
            @PathVariable int courseId) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Instructor unassigned from course",
                        courseService.unassignInstructorToCourse(courseId))
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping("/institution/search")
    public ResponseEntity<ApiResponse<Page<CourseResponseDTO>>> searchCoursesByInstitution(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Integer instructorId,
            @RequestParam(required = false) String instructorName,
            @RequestParam(defaultValue = "courseId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses fetched",
                        courseService.getCoursesScalable(
                                courseId, courseName,
                                instructorId, instructorName,
                                null, null, null,
                                sortBy, sortDirection,
                                page, size))
        );
    }

    @GetMapping("/param")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> searchCourse(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer instructorId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String instructorName) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Search results fetched",
                        courseService.searchCourse(courseId, instructorId, courseName, instructorName))
        );
    }

    @GetMapping("/name")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> searchCourseByName(
            @RequestParam String courseName) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses fetched by name",
                        courseService.searchCourseByName(courseName))
        );
    }


    @GetMapping("/counts/ratings")
    public ResponseEntity<ApiResponse<List<CourseFeedbackCountDTO>>> getFeedbackCountAndAvgRating() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Feedback analytics fetched",
                        courseService.getFeedbackCountAndAvg())
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PopularCourseDTO>>> getPopularCourses(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Popular courses fetched",
                        courseService.getPopularCourses(pageNumber, pageSize))
        );
    }

    @GetMapping("/unpopular")
    public ResponseEntity<ApiResponse<Slice<PopularCourseDTO>>> getUnPopularCourses(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Unpopular courses fetched",
                        courseService.getUnPopularCourses(pageNumber, pageSize))
        );
    }

    @GetMapping("/average")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesGreaterThanAvgRating(
            @RequestParam(defaultValue = "2.5") Double avgRating) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses filtered by average rating",
                        courseService.findCoursesLessThanAvgRating(avgRating))
        );
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesWithoutFeedbacks() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Courses without feedback fetched",
                        courseService.findCoursesWithoutFeedback())
        );
    }

    @GetMapping("/instructor")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getCoursesNotAssigned() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Unassigned courses fetched",
                        courseService.findCoursesNotAssigned())
        );
    }
}

