package com.example.userservice.controller;

import com.example.userservice.dto.CourseStatsResponse;
import com.example.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')") // ✅ Protect all endpoints in this controller
public class AdminStatsController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public AdminStatsController(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token); // ✅ Forward JWT
        return headers;
    }

    @GetMapping("/total-courses")
    public long getTotalCourses(HttpServletRequest request) {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(request));
        ResponseEntity<Long> response = restTemplate.exchange(
                "http://courseservice/api/courses/count"
                , // ✅ Call via service name
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }

    @GetMapping("/total-enrollments")
    public long getTotalEnrollments(HttpServletRequest request) {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(request));
        ResponseEntity<Long> response = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/total-enrollments", // ✅ Use service name
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }

    @GetMapping("/certificates-issued")
    public long getCertificatesIssued(HttpServletRequest request) {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(request));
        ResponseEntity<Long> response = restTemplate.exchange(
                "http://certificateservice/admin/stats/total-certificates", // ✅ Use service name
                HttpMethod.GET,
                entity,
                Long.class
        );
        return response.getBody();
    }

    @GetMapping("/total-users")
    public long getTotalUsers() {
        return userRepository.countNonAdminUsers("ADMIN");
    }

    @GetMapping("/most-popular-course")
    public CourseStatsResponse getMostPopularCourse(HttpServletRequest request) {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(request));
        ResponseEntity<CourseStatsResponse> response = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/most-popular-course", // ✅ Use service name
                HttpMethod.GET,
                entity,
                CourseStatsResponse.class
        );
        return response.getBody();
    }
}
