package com.example.contentservice.service;

import com.example.contentservice.dto.LessonProgressRequest;
import com.example.contentservice.dto.LessonProgressResponse;
import com.example.contentservice.dto.LessonWithProgressResponse;
import com.example.contentservice.model.Lesson;
import com.example.contentservice.model.LessonProgress;
import com.example.contentservice.repository.LessonProgressRepository;
import com.example.contentservice.repository.LessonRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final LessonRepository lessonRepository;
    private static final Logger log = LoggerFactory.getLogger(ProgressService.class);
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonProgressRepository progressRepository;
    private final RestTemplate restTemplate;
    public LessonProgress createProgress(LessonProgressRequest request) {
        Optional<LessonProgress> existing = lessonProgressRepository.findByUserIdAndLessonId(
                request.getUserId(), request.getLessonId()
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        LessonProgress progress = new LessonProgress();
        progress.setUserId(request.getUserId());
        progress.setLessonId(request.getLessonId());
        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        return lessonProgressRepository.save(progress);
    }

    public LessonProgressResponse trackProgress(LessonProgressRequest request, HttpServletRequest httpRequest) {
        Long userId = fetchUserIdFromUserService(httpRequest);  // Extract from JWT

        request.setUserId(userId);  // Inject the extracted ID into the request

        LessonProgress saved = createProgress(request); // Use your DRY method

        return new LessonProgressResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getLessonId(),
                saved.getIsCompleted()
        );
    }



    public List<LessonProgressResponse> getUserProgress(Long userId) {
        return progressRepository.findAllByUserId(userId)
                .stream()
                .map(progress -> new LessonProgressResponse(
                        progress.getId(),
                        progress.getUserId(),
                        progress.getLessonId(),
                        progress.getIsCompleted()
                ))
                .collect(Collectors.toList());
    }

    public void deleteProgress(Long id) {
        progressRepository.deleteById(id);
    }
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
    public double calculateCourseProgress(Long userId, Long courseId) {
        List<LessonProgress> completed = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId);
        int completedCount = completed.size();

        int totalLessons = lessonRepository.countByCourseId(courseId);  // You'll create this next

        if (totalLessons == 0) return 0.0;

        return (completedCount * 100.0) / totalLessons;
    }
    public List<LessonWithProgressResponse> getLessonsWithProgress(Long userId, Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);
        List<LessonProgress> progressList = lessonProgressRepository.findByUserIdAndCourseId(userId, courseId);

        Set<Long> completedLessonIds = progressList.stream()
                .filter(LessonProgress::getIsCompleted)
                .map(LessonProgress::getLessonId)
                .collect(Collectors.toSet());

        return lessons.stream().map(lesson -> new LessonWithProgressResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getMaterialUrl(),
                lesson.getLessonOrder(),
                completedLessonIds.contains(lesson.getId())
        )).toList();
    }
    public int calculateOverallUserProgressPercentage(Long userId) {
        // Step 1: Call enrollment service to get course IDs
        String url = "http://enrollmentservice/api/enrollments/user/" + userId + "/courses";

        try {
            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Long>>() {}
            );

            List<Long> courseIds = response.getBody();
            if (courseIds == null || courseIds.isEmpty()) {
                return 0; // No enrolled courses = 0% progress
            }

            // Step 2: Count total lessons in enrolled courses
            int totalLessons = lessonRepository.countByCourseIdIn(courseIds);

            // Step 3: Count completed lessons
            int completedLessons = lessonProgressRepository.countCompletedLessonsByUserIdAndCourseIds(userId, courseIds);

            // Step 4: Calculate percentage
            if (totalLessons == 0) return 0;
            return (int) Math.round((double) completedLessons / totalLessons * 100);
        } catch (HttpClientErrorException.Forbidden e) {
            // Log the error if access is forbidden
            log.error("Access forbidden when calling enrollment service for user {}: {}", userId, e.getMessage());
            return 0; // You might want to return 0% progress or handle it differently
        } catch (Exception e) {
            // Catch any other exceptions
            log.error("Error calculating progress for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }


}
