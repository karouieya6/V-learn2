package com.example.enrollmentservice.service;

import com.example.enrollmentservice.dto.CourseStatsResponse;
import com.example.enrollmentservice.model.Enrollment;
import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.model.EnrollmentStatus;
import com.example.enrollmentservice.repository.EnrollmentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * ✅ Enroll a user (user ID extracted from JWT)
     */
    public EnrollmentResponse enrollUser(EnrollmentRequest request, HttpServletRequest httpRequest) {
        Long userId = fetchUserIdFromUserService(httpRequest);

        // Check if user is already enrolled in this course by using only IDs
        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(userId, request.courseId());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already enrolled in this course.");
        }

        // Create and save the enrollment with userId and courseId
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(request.courseId());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("✅ User {} enrolled in course {}", userId, request.courseId());
        return mapToResponse(saved); // Map to DTO
    }

    /**
     * ✅ Unenroll a user (user ID extracted from JWT)
     */
    @Transactional
    public void unenrollUser(EnrollmentRequest request, HttpServletRequest httpRequest) {
        Long userId = fetchUserIdFromUserService(httpRequest);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, request.courseId())
                .orElseThrow(() -> {
                    log.warn("❌ Tried to unenroll non-existing enrollment: user {}, course {}", userId, request.courseId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found.");
                });

        enrollmentRepository.delete(enrollment);
        log.info("🗑️ User {} unenrolled from course {}", userId, request.courseId());
    }

    /**
     * ✅ Get all enrollments of a user
     */
    public Page<EnrollmentResponse> getUserEnrollments(Long userId, Pageable pageable) {
        return enrollmentRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);  // Just map to response using courseId and userId
    }

    /**
     * ✅ Check if a user is enrolled in a course
     */
    public boolean isUserEnrolled(Long userId, Long courseId) {
        boolean enrolled = enrollmentRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
        log.info("🔍 Enrollment check - user: {}, course: {} → enrolled: {}", userId, courseId, enrolled);
        return enrolled;
    }

    /**
     * ✅ Admin: get all enrollments
     */
    public List<EnrollmentResponse> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return enrollments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * ✅ Call UserService to extract userId from token
     */
    private Long fetchUserIdFromUserService(HttpServletRequest request) {
        String url = "http://localhost:8080/userservice/user/email";

        // this endpoint should exist and extract user ID from token

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Long.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to extract user ID from token.");
        }

        return response.getBody();
    }


    public CourseStatsResponse getMostPopularCourse() {
        List<Object[]> result = enrollmentRepository.findMostPopularCourse();

        if (result.isEmpty()) return null;

        Object[] row = result.get(0);
        Long courseId = ((Number) row[0]).longValue();
        Long count = ((Number) row[1]).longValue();

        // 🟦 Call CourseService to get title
        String courseTitle = fetchCourseTitle(courseId);

        return new CourseStatsResponse(courseId, courseTitle, count);
    }

    private String fetchCourseTitle(Long courseId) {
        String url = "http://courseservice/api/courses/" + courseId + "/title";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    /**
     * ✅ Convert Enrollment entity to DTO
     */
    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }

    public CourseStatsResponse getMostPopularCourseStats() {
        List<Object[]> result = enrollmentRepository.findMostPopularCourse();
        if (!result.isEmpty()) {
            Object[] row = result.get(0);
            return new CourseStatsResponse(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    ((Number) row[2]).longValue()
            );
        }
        return null;
    }
    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        return mapToResponse(enrollment);
    }
    public List<Long> getCourseIdsByUserId(Long userId) {
        return enrollmentRepository.findCourseIdsByUserId(userId);
    }


}
