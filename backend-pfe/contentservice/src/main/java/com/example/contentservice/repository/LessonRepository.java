package com.example.contentservice.repository;

import com.example.contentservice.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseId(Long courseId);
    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);
    int countByCourseId(Long courseId);
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.courseId = :courseId")
    int countLessonsByCourse(@Param("courseId") Long courseId);


}
