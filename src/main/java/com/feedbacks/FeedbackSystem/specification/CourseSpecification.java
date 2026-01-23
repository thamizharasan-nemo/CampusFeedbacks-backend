package com.feedbacks.FeedbackSystem.specification;

import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.Instructor;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> hasCourseId(Integer courseId){
        return ((root, query, criteriaBuilder) ->
                courseId == null ? null : criteriaBuilder.equal(root.get("courseId"), courseId));
    }

    public static Specification<Course> hasInstructorId(Integer instructorId){
        return ((root, query, criteriaBuilder) ->
                instructorId == null
                        ? null
                        : criteriaBuilder.equal(root.get("instructor").get("instructorId"), instructorId));
    }

    public static Specification<Course> hasCourseName(String courseName) {
        return ((root, query, criteriaBuilder) ->
                courseName == null || courseName.isEmpty() ? null
                        : criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("courseName")), "%"+courseName.toLowerCase()+"%"));
    }

    public static Specification<Course> hasInstructorName(String instructorName) {
        return ((root, query, criteriaBuilder) ->
                instructorName == null || instructorName.isEmpty() ? null
                        : criteriaBuilder
                        .like(criteriaBuilder.lower(root.join("instructor").get("instructorName")), "%"+instructorName.toLowerCase()+"%"));
    }

    public static Specification<Course> belongsToInstitution(Integer institutionId) {
        return (root, query, cb) ->
                cb.equal(root.get("institution").get("institutionId"), institutionId);
    }

    public static Specification<Course> coursesWithoutFeedback(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(feedbackRoot.get("course").get("courseId"));

            return criteriaBuilder.not(root.get("courseId").in(subquery));
        };
    }

    public static Specification<Course> coursesNotAssigned(){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Instructor> instructorRoot = subquery.from(Instructor.class);
            subquery.select(instructorRoot.get("courses").get("courseId"));

            return criteriaBuilder.not(root.get("courseId")).in(subquery);
        };
    }

    public static Specification<Course> greaterThanAvgRatingCourses(Double avgRating){
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
            subquery.select(criteriaBuilder.avg(feedbackRoot.get("courseRating")))
                    .where(criteriaBuilder.equal(feedbackRoot.get("course"), root));

            return criteriaBuilder.greaterThanOrEqualTo(subquery, avgRating);
        };
    }

    public static Specification<Course> popularCourse(Boolean popular){
        if (popular == null) return null;
        return (root, query, cb) -> {
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Enrollment> enrollmentRoot = subquery.from(Enrollment.class);

            subquery.select(cb.count(enrollmentRoot))
                    .where(cb.equal(enrollmentRoot.get("course"), root));
            return popular
                    ? cb.greaterThanOrEqualTo(subquery, 50L)
                    : cb.lessThan(subquery, 50L);
        };
    }
}
