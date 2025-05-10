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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CategoryRepository categoryRepository;
    @Autowired
    private HttpServletRequest request;
    private final CourseRepository courseRepository;

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

        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }

    private CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setInstructorId(course.getInstructorId());

        // 🛠 Check for null before accessing category
        if (course.getCategory() != null) {
            response.setCategoryId(course.getCategory().getId());
            response.setCategoryName(course.getCategory().getName());
        } else {
            response.setCategoryId(null);
            response.setCategoryName("Unknown"); // or leave it null
        }

        return response;
    }




    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
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
        String url = "http://localhost:8080/userservice/user/email/" + email;

        try {
            // Check if the token is in "Bearer <token>" format
            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid token format. Token must start with 'Bearer '.");
            }

            // Remove the "Bearer " prefix to get the actual token
            String actualToken = token.substring(7);

            // Set up the headers with the correct authorization format
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + actualToken);

            // Create HttpEntity with headers
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Send request and capture response
            ResponseEntity<Long> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Long.class
            );

            // Return the response body (Instructor ID)
            return response.getBody();
        } catch (HttpClientErrorException.Forbidden e) {
            throw new RuntimeException("403 Forbidden: Access denied to user service.");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("401 Unauthorized: Invalid or missing token.");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not fetch instructor ID from user service: " + e.getMessage());
        }
    }



}
