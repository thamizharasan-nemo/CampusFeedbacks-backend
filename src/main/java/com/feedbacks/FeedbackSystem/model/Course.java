package com.feedbacks.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE course SET is_deleted = true WHERE course_id = ?")
@FilterDef(name = "deletedCourseFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedCourseFilter", condition = "is_deleted = false")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int courseId;

    private String courseName;
    private String courseDescription;

    private double avgRating;
    private long feedbackCount;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String modifiedBy;

    private LocalDateTime deletedAt;
    private String deletedBy;
    private String restoredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    @JsonBackReference
    private Instructor instructor;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}

