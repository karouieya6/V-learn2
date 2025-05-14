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

    // Find enrollment by its ID
    Optional<Enrollment> findById(Long id);

    // Check if the user is already enrolled in a course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Correct method to check if the user is already enrolled in a course
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    // Paginate enrollments for a given user
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    // Correct query to count enrollments for a specific user
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Query to fetch the most popular course along with its title
    @Query(value = """
        SELECT e.course_id, c.title, COUNT(e.id) AS enrollments
        FROM enrollments e
        JOIN courses c ON e.course_id = c.id
        GROUP BY e.course_id, c.title
        ORDER BY enrollments DESC
        LIMIT 1
    """, nativeQuery = true)
    List<Object[]> findMostPopularCourse();

    // Query to count distinct students for a specific instructor using course_id and instructor_id
    @Query(value = """
    SELECT COUNT(DISTINCT e.user_id)
    FROM enrollments e
    JOIN courses c ON e.course_id = c.id
    WHERE c.instructor_id = :instructorId
    """, nativeQuery = true)
    long countDistinctStudentsByInstructorId(@Param("instructorId") Long instructorId);

    // Fetch list of course IDs by userId
    @Query("SELECT e.courseId FROM Enrollment e WHERE e.userId = :userId")
    List<Long> findCourseIdsByUserId(Long userId);

    // Query to count enrollments by instructorId using course_id and instructor_id
    @Query(value = """
    SELECT COUNT(e)
    FROM enrollments e
    JOIN courses c ON e.course_id = c.id
    WHERE c.instructor_id = :instructorId
    """, nativeQuery = true)
    long countByInstructorId(@Param("instructorId") Long instructorId);
    long countByCourseId(Long courseId);
    @Query(value = """
    SELECT e.course_id, COUNT(e.id) AS enrollments
    FROM enrollments e
    JOIN courses c ON e.course_id = c.id
    WHERE c.instructor_id = :instructorId
    GROUP BY e.course_id
    ORDER BY enrollments DESC
    LIMIT 5
""", nativeQuery = true)
    List<Object[]> findTopCoursesRawByInstructor(@Param("instructorId") Long instructorId);
    @Query(value = """
    SELECT e.user_id, MIN(e.enrolled_at) AS first_enrolled, COUNT(e.course_id) AS course_count
    FROM enrollments e
    JOIN courses c ON c.id = e.course_id
    WHERE c.instructor_id = :instructorId
    GROUP BY e.user_id
""", nativeQuery = true)
    List<Object[]> findStudentsByInstructor(@Param("instructorId") Long instructorId);

}
