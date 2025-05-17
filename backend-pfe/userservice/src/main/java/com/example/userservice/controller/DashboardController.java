package com.example.userservice.controller;
import com.example.userservice.model.Role;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.util.MultipartInputStreamFileResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import com.example.userservice.dto.CourseStatsResponse;
import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.ExcelExporter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

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
    private final RoleRepository roleRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 🧑 STUDENT DASHBOARD
    @GetMapping("/instructor/overview")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getInstructorDashboard(HttpServletRequest request, Authentication authentication) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String instructorEmail = authentication.getName();
        AppUser instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Long instructorId = instructor.getId();
        long totalCourses = 0;
        long totalEnrolledStudents = 0;
        List<CourseStatsResponse> popularCourses = new ArrayList<>();

        try {
            totalCourses = Optional.ofNullable(
                    restTemplate.exchange(
                            "http://courseservice/api/courses/instructor/" + instructorId + "/count",
                            HttpMethod.GET, entity, Long.class
                    ).getBody()
            ).orElse(0L);
        } catch (Exception e) {
            // Log error for debugging
            System.err.println("Failed to fetch course count: " + e.getMessage());
        }

        try {
            totalEnrolledStudents = Optional.ofNullable(
                    restTemplate.exchange(
                            "http://enrollmentservice/api/enrollments/instructor/" + instructorId + "/student-count",
                            HttpMethod.GET, entity, Long.class
                    ).getBody()
            ).orElse(0L);
        } catch (Exception e) {
            System.err.println("Failed to fetch student count: " + e.getMessage());
        }

        try {
            ResponseEntity<List<CourseStatsResponse>> response = restTemplate.exchange(
                    "http://enrollmentservice/api/enrollments/instructor/" + instructorId + "/most-popular-courses",
                    HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getBody() != null) {
                popularCourses = response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch popular courses: " + e.getMessage());
        }

        List<CourseStatsResponse> top5PopularCourses = popularCourses.stream()
                .sorted(Comparator.comparingLong(CourseStatsResponse::getEnrollmentCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> dashboard = Map.of(
                "totalCourses", totalCourses,
                "totalEnrolledStudents", totalEnrolledStudents,
                "top5PopularCourses", top5PopularCourses
        );

        return ResponseEntity.ok(dashboard);
    }
    @GetMapping("/instructor/courses")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getInstructorCourses(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String email = authentication.getName();
        AppUser instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Long instructorId = instructor.getId();
        List<Map<String, Object>> courses = new ArrayList<>();
        long totalCourses = 0;

        // 📦 Fetch total count of instructor's courses
        try {
            totalCourses = Optional.ofNullable(
                    restTemplate.exchange(
                            "http://courseservice/api/courses/instructor/" + instructorId + "/count",
                            HttpMethod.GET, entity, Long.class
                    ).getBody()
            ).orElse(0L);
        } catch (Exception e) {
            System.err.println("Failed to fetch course count for instructor: " + e.getMessage());
        }

        // 🎓 Fetch actual course list
        try {
            ResponseEntity<List<Map<String, Object>>> courseResponse = restTemplate.exchange(
                    "http://courseservice/api/courses/instructor/" + instructorId,
                    HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
            );
            courses = Optional.ofNullable(courseResponse.getBody()).orElse(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Failed to fetch courses from courseservice: " + e.getMessage());
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // 🔍 Filter, enrich, and collect
        for (Map<String, Object> course : courses) {
            String title = (String) course.get("title");
            Long courseId = ((Number) course.get("id")).longValue();

            if (!search.isEmpty() && (title == null || !title.toLowerCase().contains(search.toLowerCase()))) {
                continue;
            }

            int lectureCount = 0;
            long enrolledCount = 0;

            try {
                lectureCount = restTemplate.exchange(
                        "http://contentservice/api/lessons/course/" + courseId + "/count",
                        HttpMethod.GET, entity, Integer.class
                ).getBody();
            } catch (Exception e) {
                System.err.println("Error fetching lesson count for course " + courseId + ": " + e.getMessage());
            }

            try {
                enrolledCount = restTemplate.exchange(
                        "http://enrollmentservice/api/enrollments/course/" + courseId + "/count",
                        HttpMethod.GET, entity, Long.class
                ).getBody();
            } catch (Exception e) {
                System.err.println("Error fetching enrollment count for course " + courseId + ": " + e.getMessage());
            }

            result.add(Map.of(
                    "id", courseId,
                    "title", title,
                    "lectureCount", lectureCount,
                    "enrolledStudents", enrolledCount
            ));
        }

        // 🔃 Sort
        result.sort((a, b) -> {
            if ("mostPopular".equalsIgnoreCase(sortBy)) {
                return Long.compare((Long) b.get("enrolledStudents"), (Long) a.get("enrolledStudents"));
            } else {
                return Long.compare((Long) b.get("id"), (Long) a.get("id")); // Newest = higher ID
            }
        });

        // 📄 Pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, result.size());

        List<Map<String, Object>> paginatedCourses;
        if (fromIndex >= result.size()) {
            paginatedCourses = Collections.emptyList();
        } else {
            paginatedCourses = result.subList(fromIndex, toIndex);
        }

        // 📦 Final Response
        Map<String, Object> dashboard = Map.of(
                "totalCourses", totalCourses,
                "courses", paginatedCourses,
                "total", result.size(),
                "page", page,
                "size", size
        );

        return ResponseEntity.ok(dashboard);
    }

    @DeleteMapping("/instructor/courses/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> deleteCourse(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // Delete the course via course-service
            restTemplate.exchange(
                    "http://courseservice/api/courses/" + courseId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            return ResponseEntity.ok(Map.of("message", "✅ Course deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to delete course: " + e.getMessage()));
        }
    }

    @GetMapping("/instructor/students")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getStudentsOfInstructor(
            HttpServletRequest request,
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String email = authentication.getName();
        AppUser instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        Long instructorId = instructor.getId();

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "http://enrollmentservice/api/enrollments/instructor/" + instructorId + "/students",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> studentsData = response.getBody();
        if (studentsData == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch students data from Enrollment Service");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> student : studentsData) {
            Long studentId = (student.get("userId") != null) ? ((Number) student.get("userId")).longValue() : null;
            String enrolledAt = (String) student.get("enrolledAt");
            Integer courseCount = (student.get("courseCount") != null) ? ((Number) student.get("courseCount")).intValue() : 0;

            if (studentId == null || enrolledAt == null) continue;

            // 🔍 Get user profile
            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    "http://userservice/user/by-id/" + studentId,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> user = userResponse.getBody();
            if (user == null) continue;

            // 🧮 Get progress
            Integer progress = 0;
            try {
                progress = restTemplate.exchange(
                        "http://contentservice/api/lessons/progress/user/" + studentId + "/percentage",
                        HttpMethod.GET,
                        entity,
                        Integer.class
                ).getBody();
            } catch (Exception e) {
                System.err.println("Failed to fetch progress for student " + studentId + ": " + e.getMessage());
            }

            String fullName = user.getOrDefault("firstName", "") + " " + user.getOrDefault("lastName", "");

            // 🔍 Search filter
            if (!search.isBlank() && !fullName.toLowerCase().contains(search.toLowerCase())) continue;

            // ✅ Compose result
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", studentId);
            studentMap.put("name", fullName);
            studentMap.put("profileImageUrl", user.getOrDefault("profileImageUrl", null));
            studentMap.put("progress", progress);
            studentMap.put("enrolledCourses", courseCount);
            studentMap.put("enrolledDate", enrolledAt);

            result.add(studentMap);
        }

        // 🔃 Sort
        result.sort((a, b) -> {
            String dateA = (String) a.get("enrolledDate");
            String dateB = (String) b.get("enrolledDate");

            if ("oldest".equalsIgnoreCase(sortBy)) {
                return dateA.compareTo(dateB);
            } else {
                return dateB.compareTo(dateA); // Default: newest first
            }
        });

        // 📄 Paginate
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, result.size());
        if (fromIndex >= result.size()) {
            return ResponseEntity.ok(Map.of(
                    "students", Collections.emptyList(),
                    "total", result.size(),
                    "page", page,
                    "size", size
            ));
        }

        List<Map<String, Object>> paginated = result.subList(fromIndex, toIndex);

        return ResponseEntity.ok(Map.of(
                "students", paginated,
                "total", result.size(),
                "page", page,
                "size", size
        ));
    }
    @GetMapping("/instructor/courses/pending")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> getMyPendingCourses(HttpServletRequest request, Authentication authentication) {
        String email = authentication.getName();
        AppUser instructor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        Long instructorId = instructor.getId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    "http://courseservice/api/courses/instructor/" + instructorId + "/pending",
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to fetch pending courses: " + e.getMessage()));
        }
    }
    @PostMapping("/instructor/courses/create")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> courseData, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(courseData, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://courseservice/api/courses/create",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to create course: " + e.getMessage()));
        }
    }
    @PostMapping("/instructor/courses/{courseId}/upload-image")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> uploadCourseImage(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            // 🔐 Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", request.getHeader("Authorization"));

            // 📦 Wrap the file in a resource
            MultipartInputStreamFileResource fileResource =
                    new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename());

            // 🧳 Prepare multipart body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // 🌍 Forward to course service
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://courseservice/api/courses/" + courseId + "/upload-image",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // ✅ Return success
            return ResponseEntity.ok(Map.of("imageUrl", response.getBody()));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Could not read uploaded file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/instructor/courses/{courseId}/lessons")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> addLessonsToCourse(
            @PathVariable Long courseId,
            @RequestBody List<Map<String, Object>> lessons,
            HttpServletRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Enrich each lesson with courseId (optional if frontend doesn't provide it)
        lessons.forEach(lesson -> lesson.put("courseId", courseId));

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(lessons, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://contentservice/api/lessons/batch",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to add lessons: " + e.getMessage()));
        }
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
        long totalCertificates = restTemplate.exchange(
                "http://certificateservice/api/certificates/admin/stats/total-certificates",
                HttpMethod.GET, entity, Long.class).getBody();

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
                "totalCertificates", totalCertificates,
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
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Sort sort = sortBy.equalsIgnoreCase("oldest") ?
                Sort.by("createdAt").ascending() :
                Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
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
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Sort sort = sortBy.equalsIgnoreCase("oldest") ?
                Sort.by("createdAt").ascending() :
                Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
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
                    "totalStudents", studentCount,
                    "joinDate", instr.getCreatedAt()
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveInstructor(@PathVariable Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role instructorRole = roleRepository.findByName("INSTRUCTOR")
                .orElseThrow(() -> new RuntimeException("❌ Role INSTRUCTOR not found"));

        user.getRoles().add(instructorRole);

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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PutMapping("/admin/approve-course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCourse(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token); // ✅ MUST forward token
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://courseservice/api/courses/admin/approve/" + courseId,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "message", "❌ Failed to approve course: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString()
            ));
        }
    }
    @GetMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCoursesForAdmin(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getHeader("Authorization"));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    "http://courseservice/api/courses",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to fetch course list: " + e.getMessage()));
        }
    }
    @PutMapping("/admin/reject-course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCourse(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    "http://courseservice/api/courses/admin/reject/" + courseId,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            return ResponseEntity.ok(Map.of("message", "❌ Course rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to reject course: " + e.getMessage()));
        }
    }
    @GetMapping("/admin/course-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminCourseSummary(HttpServletRequest request) {
        // Create headers and entity for the request
        HttpHeaders headers = createHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        long totalCourses = 0;
        long approvedCourses = 0;
        long pendingCourses = 0;
        List<Map<String, Object>> courseList = new ArrayList<>();

        try {
            // Fetch all courses from the course-service
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "http://courseservice/api/courses",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> courses = Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());
            totalCourses = courses.size();

            // Loop through the courses to count the statuses and gather course details
            for (Map<String, Object> course : courses) {
                String status = (String) course.get("status");
                if ("APPROVED".equalsIgnoreCase(status)) {
                    approvedCourses++;
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    pendingCourses++;
                }

                // Format course summary with necessary fields
                Map<String, Object> courseMap = Map.of(
                        "title", course.get("title"),
                        "instructorName", course.get("instructorName"),  // Ensure this field is available
                        "addedDate", course.get("createdAt"),
                        "status", status
                );
                courseList.add(courseMap);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle specific client/server error exceptions
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", "❌ Failed to fetch courses from course service: " + e.getMessage()));
        } catch (Exception e) {
            // Generic error handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "❌ Failed to fetch courses: " + e.getMessage()));
        }

        // Final response structure with course statistics and list
        Map<String, Object> response = Map.of(
                "stats", Map.of(
                        "totalCourses", totalCourses,
                        "approvedCourses", approvedCourses,
                        "pendingCourses", pendingCourses
                ),
                "courses", courseList
        );

        return ResponseEntity.ok(response);
    }




}