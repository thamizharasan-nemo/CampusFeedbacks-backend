package com.feedbacks.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SQLDelete(sql = "UPDATE instructor SET is_deleted = true WHERE instructor_id = ?")
@FilterDef(name = "deletedInstructorFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedInstructorFilter", condition = "is_deleted = false")
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int instructorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private double avgRating;
    private long feedbackCount;

    private LocalDateTime deletedAt;
    private String deletedBy;
    private String restoredBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "instructor")
    @JsonManagedReference
    private List<Course> courses = new ArrayList<>();


    // helper method for adding and removing course to instructors
    public void addCourse(Course course) {
        courses.add(course);
        course.setInstructor(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setInstructor(null);
    }
}

