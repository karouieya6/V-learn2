package com.example.courseservice.service;

import com.example.courseservice.CourseSpecification;

import com.example.courseservice.dto.CourseCreateRequest;
import com.example.courseservice.dto.CourseResponse;
import com.example.courseservice.dto.CourseUpdateRequest;
import com.example.courseservice.model.Category;
import com.example.courseservice.model.Course;
import com.example.courseservice.repository.CategoryRepository;
import com.example.courseservice.repository.CourseRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CategoryRepository categoryRepository;
    @Autowired
    private HttpServletRequest request;
    private final CourseRepository courseRepository;
    @Autowired
    private RestTemplate restTemplate;

    public CourseResponse createCourse(CourseCreateRequest requestDto) {
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 🧠 Extract email and token from request
        String email = request.getUserPrincipal().getName(); // or from SecurityContext
        String token = request.getHeader("Authorization");

        // ✅ Fetch instructor ID securely
        Long instructorId = fetchInstructorIdFromUserService(email, token);

        Course course = new Course();
        course.setTitle(requestDto.getTitle());
        course.setDescription(requestDto.getDescription());
        course.setInstructorId(instructorId);
        course.setCategory(category);
        course.setStatus("PENDING");
        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }
    private CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setStatus(course.getStatus());

        if (course.getCategory() != null) {
            response.setCategoryId(course.getCategory().getId());
            response.setCategoryName(course.getCategory().getName());
        } else {
            response.setCategoryId(null);
            response.setCategoryName("Unknown");
        }

        // ✅ Fetch instructor full name from UserService
        try {
            String url = "http://localhost:8080/userservice/user/by-id/" + course.getInstructorId();
            ResponseEntity<Map> userResponse = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> user = userResponse.getBody();
            if (user != null) {
                String fullName = user.get("firstName") + " " + user.get("lastName");
                response.setInstructorName(fullName);
            } else {
                response.setInstructorName("Unknown");
            }
        } catch (Exception e) {
            response.setInstructorName("Unavailable");
        }

        return response;
    }




    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapToResponse(course);
    }


    public CourseResponse updateCourse(Long courseId, CourseUpdateRequest request, String instructorEmail, String token) {
        System.out.println("📨 Instructor email from token: " + instructorEmail);

        // ✅ Fetch instructor ID via API Gateway
        Long instructorId = fetchInstructorIdFromUserService(instructorEmail, token);
        System.out.println("🧠 Instructor ID from UserService: " + instructorId);

        // ✅ Load the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        System.out.println("📚 Course owner ID: " + course.getInstructorId());

        // 🔐 Check ownership
        if (course.getInstructorId() != instructorId)
        {
            throw new SecurityException("You are not allowed to update this course");
        }

        // ✅ Apply updates
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found")));
        course.setUpdatedAt(LocalDateTime.now());

        courseRepository.save(course);

        return mapToResponse(course);
    }




    public void deleteCourse(Long courseId, String instructorEmail, String token) {
        // ✅ Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // ✅ Fetch instructor ID via API Gateway
        Long instructorId = fetchInstructorIdFromUserService(instructorEmail,token);

        // 🔐 Check ownership
        if (course.getInstructorId() != instructorId){

            throw new SecurityException("You are not allowed to delete this course");
        }

        // ✅ Delete the course
        courseRepository.deleteById(courseId);
    }


    /**
     * ✅ Get Courses by Category ID
     */
    public List<CourseResponse> getCoursesByCategory(Long categoryId) {
        List<Course> courses = courseRepository.findByCategoryId(categoryId);
        return courses.stream()
                .map(this::mapToResponse)
                .toList();
    }
    public Page<CourseResponse> searchCourses(String keyword, Long categoryId, Long instructorId, Pageable pageable) {
        Specification<Course> spec = CourseSpecification.filterCourses(keyword, categoryId, instructorId);
        Page<Course> page = courseRepository.findAll(spec, pageable);
        return page.map(this::mapToResponse);
    }
    public Long fetchInstructorIdFromUserService(String email, String token) {
        HttpHeaders headers = new HttpHeaders();

        // Ensure token starts with "Bearer "
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }

        headers.set("Authorization", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "http://localhost:8080/userservice/user/email";

        ResponseEntity<Long> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Long.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("❌ Access denied to user service.");
        }
    }





}
