package com.example.contentservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = true;

    @Builder.Default
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();
}




