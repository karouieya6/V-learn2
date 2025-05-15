package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseCreateRequest;
import com.example.courseservice.dto.CourseResponse;
import com.example.courseservice.dto.CourseUpdateRequest;
import com.example.courseservice.model.Course;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;
    private final CourseService courseService;
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);
    private String getAuthenticatedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    @Autowired
    private HttpServletRequest httpRequest;

    /**
     * ✅ Create a New Course (Only for INSTRUCTORS)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest courseCreateRequest) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            String token = httpRequest.getHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            Long instructorId = courseService.fetchInstructorIdFromUserService(email, token);
            CourseResponse createdCourse = courseService.createCourse(courseCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/admin/stats/total-courses")
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalCourses() {
        return courseRepository.count();
    }


    @PostMapping("/{courseId}/upload-image")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<String> uploadCourseImage(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = courseService.uploadImage(courseId, file);
        return ResponseEntity.ok(imageUrl);
    }





    /**
     * ✅ Get All Courses (Public)
     */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> list = courseService.getAllCourses();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/list")
    public ResponseEntity<List<CourseResponse>> getPublicCourseList() {
        List<CourseResponse> courseList = courseService.getAllCourses();
        return ResponseEntity.ok(courseList);
    }



    /**
     * ✅ Get a Course by ID (Public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }


    /**
     * ✅ Update a Course (Only for INSTRUCTORS)
     */

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseUpdateRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String token = httpRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CourseResponse updated = courseService.updateCourse(id, request, email, token);
        return ResponseEntity.ok(updated);
    }


    /**
     * ✅ Delete a Course (Only for INSTRUCTORS)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String token = httpRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        courseService.deleteCourse(id, email, token);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable Long categoryId) {
        List<CourseResponse> courses = courseService.getCoursesByCategory(categoryId);
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/search")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<CourseResponse> courses = courseService.searchCourses(keyword, categoryId, instructorId, pageable);
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/count")
    public long countCourses() {
        return courseRepository.count();
    }
    @GetMapping("/{id}/title")
    public ResponseEntity<String> getCourseTitle(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return ResponseEntity.ok(course.getTitle());
    }
    @GetMapping("/instructor/{id}/count")
    public ResponseEntity<Long> countCoursesByInstructor(@PathVariable Long id) {
        long count = courseRepository.countByInstructorId(id);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/category/top")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTopCategory() {
        Map<String, Object> result = courseRepository.findTopCategory();
        return ResponseEntity.ok(result);
    }
    @GetMapping("/instructor/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseResponse>> getCoursesByInstructor(@PathVariable Long id) {
        log.info("Getting courses for instructor ID: {}", id);

        List<Course> courses = courseRepository.findByInstructorId(id);

        List<CourseResponse> responseList = courses.stream()
                .map(courseService::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
    @PutMapping("/admin/approve/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCourse(@PathVariable Long courseId) {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);

        if (optionalCourse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "❌ Course not found"));
        }

        Course course = optionalCourse.get();

        if ("APPROVED".equals(course.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Course is already approved"));
        }

        course.setStatus("APPROVED");
        courseRepository.save(course);

        return ResponseEntity.ok(Map.of("message", "✅ Course approved successfully"));
    }


    @GetMapping("/instructor/{id}/pending")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseResponse>> getPendingCourses(@PathVariable Long id) {
        List<Course> courses = courseRepository.findByInstructorIdAndStatus(id, "PENDING");

        List<CourseResponse> response = courses.stream()
                .map(courseService::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getAllCoursesForAdmin() {
        List<CourseResponse> list = courseService.getAllCourses(); // Assuming it returns CourseResponse
        return ResponseEntity.ok(list);
    }

}
