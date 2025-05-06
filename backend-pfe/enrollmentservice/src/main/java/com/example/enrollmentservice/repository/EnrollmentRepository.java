package com.example.enrollmentservice.repository;

import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    EnrollmentResponse getEnrollmentById(Long id);
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    @Query("SELECT e.courseId, COUNT(e.id) FROM Enrollment e GROUP BY e.courseId ORDER BY COUNT(e.id) DESC")
    List<Object[]> findMostPopularCourse();





}
