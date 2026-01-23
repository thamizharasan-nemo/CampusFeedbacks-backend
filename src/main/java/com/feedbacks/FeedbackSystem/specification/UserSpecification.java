package com.feedbacks.FeedbackSystem.specification;

import com.feedbacks.FeedbackSystem.model.Enrollment;
import com.feedbacks.FeedbackSystem.model.Feedback;
import com.feedbacks.FeedbackSystem.model.User;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> belongsToInstitution(Integer institutionId){
        return (root, query, cb) ->
                institutionId == null ? null :
                        cb.equal(root.get("institution").get("institutionId"), institutionId);
    }

    public static Specification<User> hasUserId(Integer userId){
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<User> studentById(Integer studentId){
        return (root, query, cb) ->
                studentId == null ? null :
                        cb.and(
                                cb.equal(root.get("role"), User.Role.STUDENT),
                                cb.equal(root.get("userId"), studentId)
                        );
    }

    public static Specification<User> hasStudentName(String studentName) {
        return (root, query, cb) ->
                studentName == null || studentName.isEmpty() ? null :
                        cb.like(cb.lower(root.get("username")),
                                "%" + studentName.toLowerCase() + "%");
    }

    public static Specification<User> hasRollNo(String rollNo) {
        return (root, query, cb) ->
                rollNo == null || rollNo.isEmpty() ? null :
                        cb.and(
                                cb.equal(root.get("role"), User.Role.STUDENT),
                                cb.like(cb.lower(root.get("identityNo")),
                                        "%" + rollNo.toLowerCase() + "%")
                        );
    }

    public static Specification<User> adminById(Integer adminUId){
        return (root, query, cb) ->
                adminUId == null ? null :
                        cb.and(
                                cb.equal(root.get("role"), User.Role.ADMIN),
                                cb.equal(root.get("userId"), adminUId)
                        );
    }

    public static Specification<User> hasAdminIdNo(String adminId) {
        return (root, query, cb) ->
                adminId == null || adminId.isEmpty() ? null :
                        cb.and(
                                cb.equal(root.get("role"), User.Role.ADMIN),
                                cb.like(cb.lower(root.get("identityNo")),
                                        "%" + adminId.toLowerCase() + "%")
                        );
    }

    public static Specification<User> studentsWithoutFeedback(Integer institutionId) {
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Feedback> fr = sub.from(Feedback.class);
            sub.select(fr.get("student").get("userId"));

            return cb.and(
                    cb.equal(root.get("role"), User.Role.STUDENT),
                    cb.equal(root.get("institution").get("institutionId"), institutionId),
                    cb.not(root.get("userId").in(sub))
            );
        };
    }

    public static Specification<User> studentsWithFeedback(Integer institutionId) {
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Feedback> fr = sub.from(Feedback.class);
            sub.select(fr.get("student").get("userId"));

            return cb.and(
                    cb.equal(root.get("role"), User.Role.STUDENT),
                    cb.equal(root.get("institution").get("institutionId"), institutionId),
                    root.get("userId").in(sub)
            );
        };
    }

    public static Specification<User> studentsWithoutEnrollments(Integer institutionId){
        return (root, query, cb) -> {
            Subquery<Enrollment> sub = query.subquery(Enrollment.class);
            Root<Enrollment> er = sub.from(Enrollment.class);
            sub.select(er)
                    .where(cb.equal(er.get("student"), root));

            return cb.and(
                    cb.equal(root.get("role"), User.Role.STUDENT),
                    cb.equal(root.get("institution").get("institutionId"), institutionId),
                    cb.not(cb.exists(sub))
            );
        };
    }

    public static Specification<User> studentsEnrolledToThisCourse(
            Integer courseId,
            Integer institutionId
    ){
        return (root, query, cb) -> {
            Subquery<Enrollment> sub = query.subquery(Enrollment.class);
            Root<Enrollment> er = sub.from(Enrollment.class);

            sub.select(er).where(
                    cb.equal(er.get("student"), root),
                    cb.equal(er.get("course").get("courseId"), courseId)
            );

            return cb.and(
                    cb.equal(root.get("institution").get("institutionId"), institutionId),
                    cb.exists(sub)
            );
        };
    }
}

