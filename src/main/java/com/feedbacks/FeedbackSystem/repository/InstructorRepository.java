package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.FeedbacksByInstructor;
import com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO;
import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedInstructorsDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Instructor;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Integer>,
        JpaSpecificationExecutor<Instructor> {

    Optional<Instructor> findByUser(User user);

    @EntityGraph(attributePaths = {
            "user",
            "courses",
            "courses.institution"
    })
    Optional<Instructor> findByInstructorId(int instructorId);

    @Query(value = "SELECT i.instructorId AS instructorId, " +
            "i.user.username AS instructorName, " +
            "c.courseName AS courseName, " +
            "f AS feedbacksByCourse " +
            "FROM Instructor i " +
            "LEFT JOIN i.courses c " +
            "LEFT JOIN c.feedbacks f " +
            "WHERE i.isDeleted = false " +
            "GROUP BY i.instructorId, i.user.username, c.courseName "
    )
    List<FeedbacksByInstructor> getAllFeedbacksByInstructor();

    @Query("SELECT i FROM Instructor i LEFT JOIN i.courses c WHERE c IS NULL")
    List<Instructor> findUnassignedInstructors();

    @Query(value = "SELECT * FROM Instructor WHERE is_deleted = true", nativeQuery = true)
    List<Instructor> findAllDeletedInstructor();

    @Modifying
    @Query(value = "DELETE FROM Instructor WHERE instructor_id = :instructorId", nativeQuery = true)
    void deletePermanently(@Param("instructorId")Integer instructorId);


    @Query("SELECT i.instructorId AS instructorId, " +
            "i.user.username AS instructorName, " +
            "c.courseName AS courseName, " +
            "COUNT(f) AS totalFeedbackCount, " +
            "AVG(f.instructorRating) AS avgRating " +
            "FROM Instructor i " +
            "LEFT JOIN i.courses c " +
            "LEFT JOIN c.feedbacks f " +
            "GROUP BY i.instructorId, i.user.username, c.courseName " +
            "ORDER BY avgRating DESC ")
    List<TopRatedInstructorsDTO> findTopRatedInstructor();

    // MATERIALIZED VIEW - FAST
    @Query("""
            SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO(
                    i.instructorId,
                    i.user.username,
                    i.avgRating
            )
                    FROM Instructor i
                    GROUP BY i.instructorId
                    ORDER BY i.avgRating DESC
            """)
    List<InstructorRankingDTO> getTopRatedInstructor(Pageable pageable);

    @Query("""
        SELECT i FROM Instructor i
        LEFT JOIN i.courses c
        WHERE c IS NULL
          AND i.user.institution.institutionId = :institutionId
    """)
    List<Instructor> findUnassignedInstructors(@Param("institutionId") Integer institutionId);

    @Query("""
        SELECT new com.feedbacks.FeedbackSystem.DTO.analytics.InstructorRankingDTO(
            i.instructorId,
            i.user.username,
            i.avgRating
        )
        FROM Instructor i
        WHERE i.user.institution.institutionId = :institutionId
        GROUP BY i.instructorId
        ORDER BY i.avgRating DESC
    """)
    List<InstructorRankingDTO> getTopRatedInstructor(@Param("institutionId") Integer institutionId, Pageable pageable);


    @Query("""
            SELECT COUNT(i) FROM Instructor i
            WHERE i.user.institution.institutionId = :institutionId
            """)
    Long findCountByInstitutionId(int institutionId);
}
