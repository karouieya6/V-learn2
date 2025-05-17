package com.example.enrollmentservice.controller;

import com.example.enrollmentservice.dto.ApiResponse;
import com.example.enrollmentservice.dto.CourseStatsResponse;
import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.repository.EnrollmentRepository;
import com.example.enrollmentservice.service.EnrollmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Tag(name = "Enrollments", description = "Course enrollment operations")
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    @Operation(
            summary = "Enroll user in course",
            description = "Enrolls a student or instructor in the specified course. Prevents duplicate enrollments."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @RequestBody EnrollmentRequest request,
            HttpServletRequest httpRequest) {

        EnrollmentResponse response = enrollmentService.enrollUser(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Get all enrollments",
            description = "Returns a list of all enrollments. Admin access only."
    )
    @PreAuthorize("hasRole('ADMIN')")

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        List<EnrollmentResponse> all = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(all);
    }

    @Operation(
            summary = "Unenroll user from course",
            description = "Allows a student or instructor to unenroll from a specific course."
    )
    @DeleteMapping
    public ResponseEntity<?> unenrollUser(@RequestBody EnrollmentRequest request, HttpServletRequest httpRequest) {
        enrollmentService.unenrollUser(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get user's enrollments",
            description = "Fetches paginated enrollments for a specific user (student or instructor)."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getUserEnrollments(
            @PathVariable Long userId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }



    @Operation(
            summary = "Check if user is enrolled",
            description = "Returns true/false depending on whether the user is enrolled in the given course."
    )

    @GetMapping("/check")
    public ResponseEntity<?> checkEnrollment(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.isUserEnrolled(userId, courseId);
        return ResponseEntity.ok(Map.of("enrolled", isEnrolled));
    }
    @GetMapping("/user/{userId}/count")
    public long countEnrollmentsByUser(@PathVariable Long userId) {
        return enrollmentRepository.countByUserId(userId);
    }
    @Operation(
            summary = "Get enrollment by ID",
            description = "Returns enrollment details (userId and courseId) by enrollment ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Long id) {
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }
    @GetMapping("/admin/stats/total-enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalEnrollments() {
        return enrollmentRepository.count();
    }
    @GetMapping("/user/{userId}/courses")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<Long>> getUserEnrolledCourses(@PathVariable Long userId) {
        List<Long> courseIds = enrollmentService.getCourseIdsByUserId(userId);
        return ResponseEntity.ok(courseIds);
    }

    @GetMapping("/admin/stats/most-popular-course")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public CourseStatsResponse getMostPopularCourse() {
        return enrollmentService.getMostPopularCourseStats();
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/{instructorId}/student-count")
    public ResponseEntity<Long> countStudentsByInstructor(@PathVariable Long instructorId) {
        System.out.println("Access granted!");
        return ResponseEntity.ok(enrollmentRepository.countDistinctStudentsByInstructorId(instructorId));
    }
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/{instructorId}/total")
    public ResponseEntity<Long> getTotalEnrollmentsForInstructor(@PathVariable Long instructorId) {
        return ResponseEntity.ok(enrollmentRepository.countByInstructorId(instructorId));
    }
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countEnrollmentsForCourse(@PathVariable Long courseId) {
        long count = enrollmentRepository.countByCourseId(courseId);
        return ResponseEntity.ok(count);
    }
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/{instructorId}/most-popular-courses")
    public ResponseEntity<List<CourseStatsResponse>> getTopCoursesByInstructor(@PathVariable Long instructorId) {
        List<CourseStatsResponse> courses = enrollmentService.getTopCoursesByInstructor(instructorId);
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/instructor/{instructorId}/students")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getStudentsForInstructor(@PathVariable Long instructorId) {
        List<Object[]> rawData = enrollmentRepository.findStudentsByInstructor(instructorId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            result.add(Map.of(
                    "userId", row[0],
                    "enrolledAt", row[1].toString(),
                    "courseCount", row[2]
            ));
        }

        return ResponseEntity.ok(result);
    }




}