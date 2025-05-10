package com.example.courseservice.repository;

import com.example.courseservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    List<Course> findByCategoryId(Long categoryId);
    long countByInstructorId(Long instructorId);
    @Query("SELECT c.category.name AS category, COUNT(c.id) AS total FROM Course c GROUP BY c.category.name ORDER BY total DESC LIMIT 1")
    Map<String, Object> findTopCategory();


}
