package com.example.courseservice.repository;

import com.example.courseservice.model.CourseComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {
    List<CourseComment> findByCourseId(Long courseId);
}
