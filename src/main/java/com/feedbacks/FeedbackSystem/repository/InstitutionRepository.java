package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.model.Institution;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {

    Optional<Institution> findByInstitutionCode(String institutionCode);

    boolean existsByInstitutionCode(String institutionCode);

    boolean existsByEmail(String email);

    @Query("""
        SELECT i FROM Institution i
        WHERE LOWER(i.institutionName) LIKE LOWER(CONCAT('%', :name, '%'))
        """)
    List<Institution> searchByName(@Param("name") String name);

    @Query("""
        SELECT COUNT(u.userId)
        FROM User u
        WHERE u.institution.institutionId = :institutionId
        """)
    Long totalUsersByInstitution(@Param("institutionId") Integer institutionId);

    @Query("""
        SELECT COUNT(c.courseId)
        FROM Course c
        WHERE c.institution.institutionId = :institutionId
        """)
    Long totalCoursesByInstitution(@Param("institutionId") Integer institutionId);

    @Query("""
            SELECT i FROM Institution i
            WHERE i.createdBy.userId = :userId
            """)
    Institution findByAdminId(int userId);
}

