package com.feedbacks.FeedbackSystem.model;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
@Table(
        name = "feedback",
        indexes = {
                @Index(name = "idx_feedback_course", columnList = "course_id"),
                @Index(name = "idx_feedback_instructor", columnList = "instructor_id"),
                @Index(name = "idx_feedback_submitted_at", columnList = "submitted_at"),
                @Index(name = "idx_feedback_institution", columnList = "institution_id")
        }
)
@SQLDelete(sql = "UPDATE feedback SET is_deleted = true, deleted_at = NOW() WHERE feedback_id = ?")
@FilterDef(
        name = "deletedFeedbackFilter",
        parameters = @ParamDef(name = "isDeleted", type = Boolean.class)
)
@Filter(name = "deletedFeedbackFilter", condition = "is_deleted = false")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feedbackId;

    private int courseRating;
    private int instructorRating;

    @Column(length = 1000)
    private String courseComment;

    @Column(length = 1000)
    private String instructorComment;

    private boolean anonymous;

    private LocalDateTime deletedAt;
    private String deletedBy;
    private String restoredBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        this.isDeleted = false;
    }
}


