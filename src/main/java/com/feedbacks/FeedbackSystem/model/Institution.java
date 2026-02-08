package com.feedbacks.FeedbackSystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "institution",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "institution_code"),
                @UniqueConstraint(columnNames = "email")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer institutionId;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false, unique = true)
    private String institutionCode;

    @Column(nullable = false, unique = true)
    private String email;

    private String address;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Course> courses = new ArrayList<>();

    @OneToMany
    @JsonIgnore
    private List<Instructor> instructors = new ArrayList<>();
}

