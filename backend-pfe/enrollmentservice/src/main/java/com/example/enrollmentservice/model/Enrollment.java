package com.example.enrollmentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.example.enrollmentservice.model.EnrollmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long courseId;



    private LocalDateTime enrolledAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;


}
