package com.feedbacks.FeedbackSystem.service.interfaces;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.FeedbackRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.FeedbackResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackService {
    FeedbackResponseDTO getFeedbackResponseById(int feedbackId);

    Page<FeedbackResponseDTO> getAllFeedbacks(int page,
                                              int size,
                                              String sort);

    List<FeedbackResponseDTO> getAllFeedbacksByInstituteId();

    FeedbackResponseDTO submitFeedback(@Valid FeedbackRequestDTO feedbackRequestDTO);

    FeedbackResponseDTO editFeedback(int feedbackId, @Valid FeedbackRequestDTO feedbackRequestDTO);

    void deleteFeedbackById(int feedbackId);

    FeedbackResponseDTO restoreFeedback(int feedbackId);

    void deleteFeedbackPermanently(int feedbackId);

    List<FeedbackResponseDTO> findAllSoftDeletedFeedbacks();

    List<FeedbackResponseDTO> getFeedbacksByStudentAndCourse(int courseId);

    Sort sortingFunction(String sort);

    Page<FeedbackResponseDTO> getSortedFeedbackByCourseId(int courseId,
                                                          int page,
                                                          int size,
                                                          String sort);

    Page<FeedbackResponseDTO> getFeedbackByStudent(int page,
                                                   int size,
                                                   String sort);


    List<FeedbackResponseDTO> getFilteredFeedback(Integer courseId,
                                                  Integer minRating,
                                                  Integer maxRating,
                                                  LocalDateTime fromDate,
                                                  LocalDateTime toDate,
                                                  Boolean anonymous
    );

    List<FeedbackResponseDTO> searchFeedback(Integer courseId,
                                             Integer studentId,
                                             Integer minRating,
                                             String keyword,
                                             String studentName,
                                             Boolean anonymous,
                                             LocalDateTime fromDate,
                                             LocalDateTime toDate
    );


    List<FeedbackResponseDTO> getFeedbacksByEnrollment(int enrollmentId);

    List<FeedbackResponseDTO> getRecentFeedbacksByCourseId(int courseId, int limit);

    Double getAverageCourseRating(int courseId);

    Double getAverageInstructorRating(int instructorId);

    List<CourseFeedbackSummary> getCourseSummary();

    List<FeedbackTrendDTO> getFeedbackTrends(String groupBy);

    List<RatingDistributionDTO> getFeedbackRatings();

    Long countTotalFeedbacksByInstitution();
}
