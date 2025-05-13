package com.example.userservice.controller;

import com.example.userservice.dto.CourseStatsResponse;
import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.ExcelExporter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 🧑 STUDENT DASHBOARD
    @GetMapping("/instructor/overview")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getInstructorDashboard(HttpServletRequest request, Authentication authentication) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Fetch the authenticated instructor's ID
        String instructorEmail = authentication.getName();
        AppUser instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Fetch the total number of courses taught by the instructor
        long totalCourses = restTemplate.exchange(
                "http://courseservice/api/courses/instructor/" + instructor.getId() + "/count",
                HttpMethod.GET, entity, Long.class).getBody();

        // Fetch the total number of students enrolled in the instructor's courses
        long totalEnrolledStudents = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/instructor/" + instructor.getId() + "/student-count",
                HttpMethod.GET, entity, Long.class).getBody();

        // Fetch the instructor's courses sorted by the number of students enrolled
        List<CourseStatsResponse> popularCourses = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/instructor/" + instructor.getId() + "/most-popular-courses",
                HttpMethod.GET, entity, List.class).getBody();

        // Get the top 5 most popular courses based on student enrollments
        List<CourseStatsResponse> top5PopularCourses = popularCourses.stream()
                .sorted((course1, course2) -> Long.compare(course2.getEnrollmentCount(), course1.getEnrollmentCount()))
                .limit(5)
                .collect(Collectors.toList());

        // Prepare and return the instructor's dashboard data
        return ResponseEntity.ok(Map.of(
                "totalCourses", totalCourses,
                "totalEnrolledStudents", totalEnrolledStudents,
                "top5PopularCourses", top5PopularCourses
        ));
    }

    // 👑 ADMIN DASHBOARD
    private HttpHeaders createHeaders(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return headers;
    }

    @GetMapping("/admin/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminDashboard(HttpServletRequest request) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        long totalCourses = restTemplate.exchange(
                "http://courseservice/api/courses/count",
                HttpMethod.GET, entity, Long.class).getBody();

        long totalEnrollments = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/total-enrollments",
                HttpMethod.GET, entity, Long.class).getBody();

        long totalUsers = userRepository.countNonAdminUsers("ADMIN");

        CourseStatsResponse mostPopularCourse = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/most-popular-course",
                HttpMethod.GET, entity, CourseStatsResponse.class).getBody();

        long requests = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM instructor_requests", Long.class);
        long approved = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM instructor_requests WHERE status = 'APPROVED'", Long.class);
        long activeInstructors = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT instructor_id) FROM courses", Long.class);

        List<AppUser> instructors = userRepository.findAllByRole("INSTRUCTOR");
        List<Map<String, Object>> instructorLoad = new ArrayList<>();
        List<Map<String, Object>> topInstructors = new ArrayList<>();

        for (AppUser instr : instructors) {
            long courseCount = restTemplate.exchange(
                    "http://courseservice/api/courses/instructor/" + instr.getId() + "/count",
                    HttpMethod.GET, entity, Long.class).getBody();

            long studentCount = restTemplate.exchange(
                    "http://enrollmentservice/api/enrollments/instructor/" + instr.getId() + "/student-count",
                    HttpMethod.GET, entity, Long.class).getBody();

            Map<String, Object> data = Map.of(
                    "name", instr.getUsername(),
                    "email", instr.getEmail(),
                    "courseCount", courseCount,
                    "studentCount", studentCount
            );

            instructorLoad.add(data);

            // Only consider instructors with students for top list
            if (studentCount > 0) {
                topInstructors.add(data);
            }
        }

// Sort by studentCount descending
        topInstructors.sort((a, b) -> Long.compare((Long) b.get("studentCount"), (Long) a.get("studentCount")));

// Return top 5
        List<Map<String, Object>> topFive = topInstructors.subList(0, Math.min(5, topInstructors.size()));




        return ResponseEntity.ok(Map.of(
                "totalCourses", totalCourses,
                "totalEnrollments", totalEnrollments,
                "totalUsers", totalUsers,
                "mostPopularCourse", mostPopularCourse,
                "instructorFunnel", Map.of(
                        "totalRequests", requests,
                        "approved", approved,
                        "activeInstructors", activeInstructors
                ),
                "instructorLoad", instructorLoad,
                "topInstructors", topFive
        ));

    }
    @GetMapping("/admin/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStudents(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> students = userRepository.findByRoleAndUsernameContainingIgnoreCase("STUDENT", search, pageable);


        List<Map<String, Object>> result = new ArrayList<>();
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (AppUser student : students) {
            long totalCourses = 0;
            int progress = 0;

            try {
                totalCourses = restTemplate.exchange(
                        "http://enrollmentservice/api/enrollments/user/" + student.getId() + "/count",
                        HttpMethod.GET, entity, Long.class).getBody();
            } catch (Exception ignored) {}

            try {
                progress = restTemplate.exchange(
                        "http://contentservice/api/lessons/progress/user/" + student.getId() + "/percentage",
                        HttpMethod.GET, entity, Integer.class).getBody();
            } catch (Exception ignored) {}

            result.add(Map.of(
                    "id", student.getId(),
                    "username", student.getUsername(),
                    "email", student.getEmail(),
                    "totalCourses", totalCourses,
                    "progress", progress,
                    "joinDate", student.getCreatedAt()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "students", result,
                "totalPages", students.getTotalPages(),
                "currentPage", students.getNumber()
        ));
    }


    @DeleteMapping("/admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, Authentication authentication) {
        String adminEmail = authentication.getName();

        int roleCount = userService.getRoleCount(id);
        if (roleCount > 1) {
            boolean removed = userService.removeRoleFromUser(id, "STUDENT");
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "✅ Student role removed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "❌ Student role not found for user"));
            }
        } else {
            userService.deleteUser(id, adminEmail);
            return ResponseEntity.ok(Map.of("message", "✅ User deleted (only had STUDENT role)"));
        }
    }
    @GetMapping("/admin/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getInstructors(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> instructors = userRepository.findByRoleAndUsernameContainingIgnoreCase("INSTRUCTOR", search, pageable);


        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        List<Map<String, Object>> result = new ArrayList<>();

        for (AppUser instr : instructors) {
            long totalCourses = 0, studentCount = 0;
            try {
                totalCourses = restTemplate.exchange(
                        "http://courseservice/api/courses/instructor/" + instr.getId() + "/count",
                        HttpMethod.GET, entity, Long.class).getBody();
                studentCount = restTemplate.exchange(
                        "http://enrollmentservice/api/enrollments/instructor/" + instr.getId() + "/student-count",
                        HttpMethod.GET, entity, Long.class).getBody();
            } catch (Exception ignored) {}

            result.add(Map.of(
                    "id", instr.getId(),
                    "username", instr.getUsername(),
                    "email", instr.getEmail(),
                    "totalCourses", totalCourses,
                    "totalStudents", studentCount
            ));
        }

        return ResponseEntity.ok(Map.of(
                "instructors", result,
                "totalPages", instructors.getTotalPages(),
                "currentPage", instructors.getNumber()
        ));
    }


    @DeleteMapping("/admin/instructors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteInstructor(@PathVariable Long id, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            Long courseCount = restTemplate.exchange(
                    "http://courseservice/api/courses/instructor/" + id + "/count",
                    HttpMethod.GET, entity, Long.class).getBody();

            Long studentCount = restTemplate.exchange(
                    "http://enrollmentservice/api/enrollments/instructor/" + id + "/student-count",
                    HttpMethod.GET, entity, Long.class).getBody();

            if ((courseCount != null && courseCount > 0) || (studentCount != null && studentCount > 0)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "❌ Cannot delete instructor with active courses or students."));
            }

            boolean removed = userService.removeRoleFromUser(id, "INSTRUCTOR");
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "✅ Instructor role removed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "❌ Instructor role not found for user"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Error removing instructor role: " + e.getMessage()));
        }
    }
    @PutMapping("admin/approve-instructor/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> approveInstructor(@PathVariable Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().add("INSTRUCTOR");
        user.setForceReLogin(true);
        userRepository.save(user);

        // ✅ Delete the request from instructor_requests after promotion
        jdbcTemplate.update("DELETE FROM instructor_requests WHERE user_id = ?", userId);

        return ResponseEntity.ok(Map.of("message", "✅ User promoted to INSTRUCTOR and request deleted"));
    }


    /**
     * ✅ Get instructor requests (Admin only)
     */
    @GetMapping("admin/instructor-requests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getInstructorRequests(
            @RequestParam(defaultValue = "") String search, // Search by username
            @RequestParam(defaultValue = "newest") String sortBy // Sort by "newest" or "oldest"
    ) {
        // Base query for fetching instructor requests with status 'PENDING'
        String sql = """
    SELECT u.id, u.email, u.username, ir.status, ir.created_at
    FROM instructor_requests ir
    JOIN users u ON u.id = ir.user_id
    WHERE ir.status = 'PENDING'
    """;

        // Prepare the parameters list
        List<Object> params = new ArrayList<>();

        // Add search condition if the search term is provided
        if (!search.isEmpty()) {
            sql += " AND u.username LIKE ?";
            params.add("%" + search + "%"); // Wildcard for searching by username
        }

        // Sort by creation date - newest or oldest
        if ("oldest".equalsIgnoreCase(sortBy)) {
            sql += " ORDER BY ir.created_at ASC"; // Sort by oldest
        } else {
            sql += " ORDER BY ir.created_at DESC"; // Default: Sort by newest
        }

        // Execute the query with the parameters
        List<Map<String, Object>> requests = jdbcTemplate.queryForList(sql, params.toArray());

        return ResponseEntity.ok(requests);
    }


    @PutMapping("admin/reject-instructor/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> rejectInstructor(@PathVariable Long userId) {
        int updated = jdbcTemplate.update("UPDATE instructor_requests SET status = 'REJECTED' WHERE user_id = ?", userId);
        if (updated > 0) {
            return ResponseEntity.ok(Map.of("message", "❌ Instructor request rejected"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Request not found"));
        }
    }
    @GetMapping("/admin/course-engagement-export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> exportCourseEngagement(HttpServletRequest request) {

        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Fetch total courses
        long totalCourses = restTemplate.exchange(
                "http://courseservice/api/courses/count",
                HttpMethod.GET, entity, Long.class).getBody();

        // Fetch total enrollments
        long totalEnrollments = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/total-enrollments",
                HttpMethod.GET, entity, Long.class).getBody();

        // Fetch most popular course
        CourseStatsResponse mostPopularCourse = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/admin/stats/most-popular-course",
                HttpMethod.GET, entity, CourseStatsResponse.class).getBody();

        // Fetch active instructors
        long activeInstructors = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT instructor_id) FROM courses", Long.class);

        // Fetch active students
        long activeStudents = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT user_id) FROM enrollments WHERE status = 'ENROLLED'", Long.class);

        // Prepare data for Excel export
        List<String[]> rows = List.of(
                new String[] { "Metric", "Value" },
                new String[] { "Total Active Courses", String.valueOf(totalCourses) },
                new String[] { "Total Enrollments", String.valueOf(totalEnrollments) },
                new String[] { "Most Popular Course", mostPopularCourse != null ? mostPopularCourse.getCourseTitle() : "N/A" },
                new String[] { "Active Instructors", String.valueOf(activeInstructors) },
                new String[] { "Active Students", String.valueOf(activeStudents) }
        );

        // Generate the Excel file
        byte[] excelData;
        try {
            excelData = ExcelExporter.generateDashboardStatsExcel(rows);
        } catch (IOException e) {
            // Provide a detailed error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InputStreamResource(new ByteArrayInputStream("Error generating Excel file.".getBytes())));
        }

        // Convert byte[] to InputStreamResource for download
        ByteArrayInputStream inputStream = new ByteArrayInputStream(excelData);
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Set headers for file download
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Disposition", "attachment; filename=course_engagement_report.xlsx");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }




}