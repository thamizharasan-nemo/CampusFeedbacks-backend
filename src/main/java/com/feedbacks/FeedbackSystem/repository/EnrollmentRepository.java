package com.feedbacks.FeedbackSystem.repository;

import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    boolean existsByCourseAndStudent(Course course, User student);

    boolean existsByCourse_CourseIdAndStudent_UserIdAndInstitution_InstitutionId(
            int courseId,
            int studentId,
            int institutionId
    );

    List<Enrollment> findByStudent(User student);

    @Query("""
            SELECT e FROM Enrollment e
            WHERE e.institution.institutionId = :institutionId
            """)
    Page<Enrollment> findByInstitutionId(@Param("institutionId") int institutionId, Pageable pageable);

    Enrollment findByStudent_UserIdAndCourse_CourseIdAndInstitution_InstitutionId(int studentId,
                                                                                  int courseId,
                                                                                  int institutionId);

    List<Enrollment> findByStudent_IdentityNoAndInstitution_InstitutionId(String identityNo, int institutionId);

    List<Enrollment> findByInstitution_InstitutionId(int institutionId);

    @Query("""
        SELECT COUNT(e)
        FROM Enrollment e
        WHERE e.course.courseId = :courseId
          AND e.institution.institutionId = :institutionId
    """)
    Integer getCourseEnrollmentCount(@Param("courseId") int courseId, @Param("institutionId") int institutionId);

    @Query("""
        SELECT COUNT(e)
        FROM Enrollment e
        WHERE e.student.userId = :studentId
          AND e.institution.institutionId = :institutionId
    """)
    Integer countStudentTotalEnrollmentsByInstitution(@Param("studentId") int studentId,
                                                      @Param("institutionId") int institutionId);

    long countByInstitution_InstitutionId(int institutionId);

}
