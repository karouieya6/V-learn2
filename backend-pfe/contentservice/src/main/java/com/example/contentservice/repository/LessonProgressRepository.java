package com.example.contentservice.repository;

import com.example.contentservice.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);

    // new:
    @Query("""
      SELECT p 
        FROM LessonProgress p
        JOIN Lesson l ON p.lessonId = l.id
       WHERE p.userId = :userId
         AND l.courseId = :courseId
       ORDER BY l.lessonOrder
    """)
    List<LessonProgress> findByUserIdAndCourseId(Long userId, Long courseId);

    List<LessonProgress> findAllByUserId(Long userId);
    @Query("""
    SELECT COUNT(p)
    FROM LessonProgress p
    JOIN Lesson l ON p.lessonId = l.id
    WHERE p.userId = :userId AND l.courseId = :courseId AND p.isCompleted = true
""")
    int countCompletedLessons(@Param("userId") Long userId, @Param("courseId") Long courseId);


    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.courseId = :courseId")
    int countLessonsByCourse(@Param("courseId") Long courseId);

}
