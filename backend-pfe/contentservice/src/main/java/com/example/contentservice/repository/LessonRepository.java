package com.example.contentservice.repository;

import com.example.contentservice.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseId(Long courseId);

    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    int countByCourseId(Long courseId);

    // ✅ Used for calculating user-wide progress
    int countByCourseIdIn(List<Long> courseIds);
}
