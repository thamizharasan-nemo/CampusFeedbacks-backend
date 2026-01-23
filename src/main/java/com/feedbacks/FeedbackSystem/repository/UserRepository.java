package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.DTO.analytics.TopRatedStudentsDTO;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.identityNo = :identityNo")
    Optional<User> findByIdentityNo(String identityNo);

    boolean existsByEmailAndUserIdNot(String email, Integer userId);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.Role role);

    @Query(value = "DELETE FROM User WHERE user_id = :userId", nativeQuery = true)
    void deleteUserById(@Param("userId") int userId);

    // FIXED
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = STUDENT")
    Integer totalStudentsCount();

    @Query("""
            SELECT u FROM User u
            WHERE u.institution.institutionId = :institutionId
            """)
    Page<User> findAllUserInInstitution(int institutionId, Pageable pageable);

    // Institution scoped
    @Query("""
        SELECT u FROM User u
        WHERE u.institution.institutionId = :institutionId
        AND u.role = :role
    """)
    List<User> findByInstitutionAndRole(Integer institutionId, User.Role role);

    Optional<User> findByIdentityNoAndInstitution_InstitutionId(String identityNo, Integer institutionId);

    // Top Students by Institution
    @Query("""
        SELECT u.userId AS studentId,
               u.username AS studentName,
               u.identityNo AS identityNo,
               COUNT(f) AS feedbackCount
        FROM User u
        LEFT JOIN Feedback f ON u.userId = f.student.userId
        WHERE u.role = STUDENT
          AND u.institution.institutionId = :institutionId
        GROUP BY u.userId, u.username, u.identityNo
        ORDER BY feedbackCount DESC
    """)
    List<TopRatedStudentsDTO> findTopStudentsByFeedbacks(
            @Param("institutionId") Integer institutionId,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.institution.institutionId = :institutionId
        AND u.role = STUDENT
    """)
    Integer countStudentsByInstitution(Integer institutionId);
}

