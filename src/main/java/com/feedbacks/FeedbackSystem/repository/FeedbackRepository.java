package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.CourseFeedbackSummary;
import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO;
import com.feedbacks.FeedbackSystem.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer>,
        JpaSpecificationExecutor<Feedback> {

    @Query("SELECT f FROM Feedback f WHERE f.institution.institutionId = :institutionId")
    Page<Feedback> findAllFeedbacks(@Param("institutionId") int institutionId, Pageable pageable);

    Page<Feedback> findByCourse_CourseIdAndInstitution_InstitutionId(int courseId,
                                                                     int institutionId,
                                                                     Pageable pageable);

    @Query("""
                SELECT f FROM Feedback f
                WHERE f.instructor.instructorId = :instructorId
                AND f.institution.institutionId = :institutionId
            """)
    Page<Feedback> findAllByInstructor_InstructorIdAndInstitutionId(@Param("instructorId")int instructorId, @Param("institutionId") int institutionId, Pageable pageable);

    @Query("SELECT f FROM Feedback f WHERE f.institution.institutionId = :institutionId")
    List<Feedback> getAllFeedbacksByInstituteId(@Param("institutionId") int institutionId);

    @Query("""
            SELECT f FROM Feedback f 
            WHERE f.student.userId = :userId 
            AND f.institution.institutionId = :institutionId
            """)
    Page<Feedback> findByStudent_UserIdAndInstitution_InstitutionId(int userId, int institutionId, Pageable pageable);

    List<Feedback> findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(int userId,
                                                                                      int courseId,
                                                                                      int institutionId);

    @Query("""
                SELECT f FROM Feedback f
                WHERE (:courseId IS NULL OR f.course.courseId = :courseId)
                  AND (:minRating IS NULL OR f.courseRating >= :minRating)
                  AND (:maxRating IS NULL OR f.courseRating <= :maxRating)
                  AND (:fromDate IS NULL OR f.submittedAt >= :fromDate)
                  AND (:toDate IS NULL OR f.submittedAt <= :toDate)
                  AND (:anonymous IS NULL OR f.anonymous = :anonymous)
                  AND f.institution.institutionId = :institutionId
            """)
    List<Feedback> filterFeedback(
            Integer courseId,
            Integer minRating,
            Integer maxRating,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Boolean anonymous,
            int institutionId
    );


    @Query("SELECT f FROM Feedback f WHERE f.isDeleted = true AND f.institution.institutionId = :institutionId")
    List<Feedback> findAllDeletedFeedback(int institutionId);

    @Modifying
    @Query("""
                UPDATE Feedback f
                SET f.isDeleted = true,
                    f.deletedAt = CURRENT_TIMESTAMP,
                    f.deletedBy = :name
                WHERE f.course.courseId = :courseId
                  AND f.institution.institutionId = :institutionId
            """)
    void softDeleteByCourse(int courseId, int institutionId, String name);

    @Modifying
    @Query("""
                UPDATE Feedback f
                SET f.isDeleted = false,
                    f.deletedAt = null,
                    f.deletedBy = null,
                    f.restoredBy = :name
                WHERE f.course.courseId = :courseId
                  AND f.institution.institutionId = :institutionId
            """)
    void restoreByCourse(int courseId, int institutionId, String name);

    @Modifying
    @Query("""
                DELETE FROM Feedback f
                WHERE f.feedbackId = :feedbackId
                  AND f.institution.institutionId = :institutionId
            """)
    void deletePermanently(int feedbackId, int institutionId);

    @Query("""
            SELECT f.course.courseId AS courseId,
                   f.course.courseName AS courseName,
                   f.instructor.user.username AS instructorName,
                   (AVG(f.courseRating) + AVG(f.instructorRating)) / 2 AS averageRating,
                   COUNT(f.feedbackId) AS feedbackCount
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
            GROUP BY f.course.courseId, f.course.courseName, f.instructor.user.username
            """)
    List<CourseFeedbackSummary> findCourseSummariesByInstitution(int institutionId);


    @Query("""
                SELECT AVG(f.courseRating)
                FROM Feedback f
                WHERE f.course.courseId = :courseId
                  AND f.institution.institutionId = :institutionId
            """)
    Double getAvgCourseRating(int courseId, int institutionId);

    @Query("""
                SELECT AVG(f.instructorRating)
                FROM Feedback f
                WHERE f.instructor.instructorId = :instructorId
                  AND f.institution.institutionId = :institutionId
            """)
    Double getAvgInstructorRating(int instructorId, int institutionId);



    @Query("""
            SELECT f FROM Feedback f
            WHERE f.course.courseId = :courseId
              AND f.institution.institutionId = :institutionId
              AND f.submittedAt >= :fromDate
            ORDER BY f.submittedAt DESC
            """)
    List<Feedback> getRecentFeedbacksByCourseIdAndInstitutionId(int courseId, int institutionId, LocalDateTime fromDate);

    @Query("""
                SELECT COUNT(f)
                FROM Feedback f
                WHERE f.student.userId = :userId
                  AND f.institution.institutionId = :institutionId
                  AND f.submittedAt >= :startOfDay
            """)
    int countTodayFeedbacks(int userId, int institutionId, LocalDateTime startOfDay);

    @Query("""
                SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.RatingDistributionDTO(
                    f.courseRating,
                    COUNT(f)
                )
                FROM Feedback f
                WHERE f.institution.institutionId = :institutionId
                GROUP BY f.courseRating
                ORDER BY f.courseRating DESC
            """)
    List<RatingDistributionDTO> getRatingDistribution(int institutionId);



    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                CAST(FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m') AS string),
                COUNT(f)
            )
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
            GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%m-%d')
            ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%m-%d')
            """)
    List<FeedbackTrendDTO> getDailyTrendsByInstitutionId(int institutionId);


    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                CAST(FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m') AS string),
                COUNT(f)
            )
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
            GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m')
            ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m')
            """)
    List<FeedbackTrendDTO> getMonthlyTrendsByInstitutionId(int institutionId);


    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.FeedbackTrendDTO(
                CAST(FUNCTION('DATE_FORMAT', f.submittedAt, '%Y-%m') AS string),
                COUNT(f)
            )
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
            GROUP BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y')
            ORDER BY FUNCTION('DATE_FORMAT', f.submittedAt, '%Y')
            """)
    List<FeedbackTrendDTO> getYearlyTrendsByInstitutionId(int institutionId);


    @Query("""
            SELECT COALESCE(AVG(f.instructorRating), 0.0)
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
              AND f.submittedAt >= :fromDate
            """)
    Double avgInstructorRatingLast7Days(int institutionId, LocalDateTime fromDate);

    @Query("""
            SELECT COALESCE(AVG(f.courseRating), 0.0)
            FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
              AND f.submittedAt >= :fromDate
            """)
    Double avgCourseRatingLast7Days(int institutionId, LocalDateTime fromDate);


    @Query("""
                SELECT f FROM Feedback f
                WHERE f.course.courseId = :courseId
                  AND f.institution.institutionId = :institutionId
                  AND f.submittedAt >= :from
                ORDER BY f.submittedAt DESC
            """)
    List<Feedback> getRecentFeedbacksByCourse(int courseId, int institutionId, LocalDateTime from);

    @Query("""
            SELECT COUNT(f) FROM Feedback f
            WHERE f.institution.institutionId = :institutionId
            """)
    Long findCountByInstitutionId(int institutionId);
}
