package com.example.userservice.controller;

import com.example.userservice.dto.CourseStatsResponse;
import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.example.userservice.util.ExcelExporter;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')") // ✅ Protect all endpoints in this controller
public class AdminStatsController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStats(HttpServletRequest request,
                                              @RequestParam(defaultValue = "xlsx") String format) throws IOException {
        if (!format.equalsIgnoreCase("xlsx")) {
            return ResponseEntity.badRequest().body("Only Excel format (xlsx) is supported.".getBytes());
        }

        List<String[]> rows = List.of(
                new String[] {"Metric", "Value"},
                new String[] {"Total Users", String.valueOf(getTotalUsers())},
                new String[] {"Total Courses", String.valueOf(getTotalCourses(request))},
                new String[] {"Total Enrollments", String.valueOf(getTotalEnrollments(request))},
                new String[] {"Most Popular Course", getMostPopularCourse(request).getCourseTitle()}
                // More rows like instructor funnel/load can be added later
        );

        byte[] excelFile = ExcelExporter.generateDashboardStatsExcel(rows);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard_stats.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }



    @GetMapping("/instructor-load")
    public List<Map<String, Object>> getInstructorLoad(HttpServletRequest request) {
        List<AppUser> instructors = userRepository.findAllByRole("INSTRUCTOR");

        List<Map<String, Object>> result = new ArrayList<>();
        for (AppUser instr : instructors) {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            long courseCount = restTemplate.exchange(
                    "http://courseservice/api/courses/instructor/" + instr.getId() + "/count",
                    HttpMethod.GET,
                    entity,
                    Long.class
            ).getBody();

            long studentCount = restTemplate.exchange(
                    "http://enrollmentservice/api/enrollments/instructor/" + instr.getId() + "/student-count",
                    HttpMethod.GET,
                    entity,
                    Long.class
            ).getBody();

            result.add(Map.of(
                    "instructor", instr.getUsername(),
                    "courses", courseCount,
                    "students", studentCount
            ));
        }
        return result;
    }

    @GetMapping("/instructor-funnel")
    public Map<String, Object> getInstructorFunnel() {
        long requests = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM instructor_requests", Long.class);
        long approved = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM instructor_requests WHERE status = 'APPROVED'", Long.class);
        long activeInstructors = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT instructor_id) FROM courses", Long.class);

        return Map.of(
                "totalRequests", requests,
                "approved", approved,
                "activeInstructors", activeInstructors
        );
    }

}
