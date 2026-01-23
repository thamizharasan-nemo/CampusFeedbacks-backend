package com.feedbacks.FeedbackSystem.specification;

import com.feedbacks.FeedbackSystem.model.Feedback;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeedbackSpecification {

    public static Specification<Feedback> hasCourseId(Integer courseId) {
        return (root, query, cb) ->
                courseId == null
                        ? null
                        : cb.equal(
                        root.get("course").get("courseId"),
                        courseId
                );
    }

    public static Specification<Feedback> hasStudentId(Integer studentId) {
        return (root, query, cb) ->
                studentId == null
                        ? null
                        : cb.equal(
                        root.get("student").get("userId"),
                        studentId
                );
    }

    public static Specification<Feedback> courseRatingGreaterThan(Integer minRating) {
        return (root, query, cb) ->
                minRating == null
                        ? null
                        : cb.greaterThanOrEqualTo(
                        root.get("courseRating"),
                        minRating
                );
    }

    public static Specification<Feedback> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("courseComment")), pattern),
                    cb.like(cb.lower(root.get("instructorComment")), pattern)
            );
        };
    }

    public static Specification<Feedback> hasStudentName(String studentName) {
        return (root, query, cb) ->
                studentName == null || studentName.isBlank()
                        ? null
                        : cb.like(
                        cb.lower(root.join("student").get("username")),
                        "%" + studentName.toLowerCase() + "%"
                );
    }

    public static Specification<Feedback> feedbackSubmittedBetween(
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        return (root, query, cb) -> {
            if (fromDate != null && toDate != null) {
                return cb.between(root.get("submittedAt"), fromDate, toDate);
            }
            if (fromDate != null) {
                return cb.greaterThanOrEqualTo(root.get("submittedAt"), fromDate);
            }
            if (toDate != null) {
                return cb.lessThanOrEqualTo(root.get("submittedAt"), toDate);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Feedback> belongsToInstitution(int institutionId) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("institution").get("institutionId"),
                        institutionId
                );
    }


    public static Specification<Feedback> anonymousFeedbacks(Boolean anonymous) {
        return (root, query, cb) ->
                anonymous == null
                        ? null
                        : cb.equal(root.get("anonymous"), anonymous);
    }
}
