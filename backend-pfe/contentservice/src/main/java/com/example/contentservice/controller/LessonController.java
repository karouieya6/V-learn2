package com.example.contentservice.controller;

import com.example.contentservice.dto.*;
import com.example.contentservice.model.LessonProgress;
import com.example.contentservice.repository.LessonProgressRepository;
import com.example.contentservice.repository.LessonRepository;
import com.example.contentservice.service.LessonService;
import com.example.contentservice.service.ProgressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private static final Logger log = LoggerFactory.getLogger(LessonController.class);

    private final LessonService lessonService;
    private final ProgressService progressService;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    // ✅ Create Lesson (INSTRUCTOR only)
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a new lesson", responses = {
            @ApiResponse(responseCode = "200", description = "Lesson created successfully")
    })
    public ResponseEntity<LessonResponse> createLesson(@RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.createLesson(request));
    }

    // ✅ Get All Lessons (STUDENT, INSTRUCTOR, ADMIN)
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @Operation(summary = "Get all lessons", responses = {
            @ApiResponse(responseCode = "200", description = "Lessons retrieved")
    })
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    // ✅ Get Lesson by ID (STUDENT, INSTRUCTOR, ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    @Operation(summary = "Get lesson by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Lesson found")
    })
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    // ✅ Delete Lesson (INSTRUCTOR, ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
    @Operation(summary = "Delete a lesson", responses = {
            @ApiResponse(responseCode = "200", description = "Lesson deleted successfully")
    })
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok().build();
    }

    // ✅ Track Lesson Progress (STUDENT only)
    @PostMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LessonProgressResponse> trackProgress(
            @RequestBody LessonProgressRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(progressService.trackProgress(request, httpRequest));
    }


    // ✅ Get Progress by User (STUDENT, ADMIN)
    @GetMapping("/progress/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @Operation(summary = "Get user progress", responses = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved")
    })
    public ResponseEntity<List<LessonProgressResponse>> getUserProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }

    // ✅ Delete Progress Entry (ADMIN only)
    @DeleteMapping("/progress/{progressId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete progress entry", responses = {
            @ApiResponse(responseCode = "200", description = "Progress entry deleted")
    })
    public ResponseEntity<Void> deleteProgress(@PathVariable Long progressId) {
        progressService.deleteProgress(progressId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{lessonId}/upload-material")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<String> uploadMaterial(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file) {
        String fileUrl = lessonService.uploadMaterial(lessonId, file);
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/materials/{filename:.+}")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable String filename) throws MalformedURLException {
        Path filePath = Paths.get("uploads/materials/").resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','ADMIN')")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(
            @PathVariable Long courseId
    ) {
        List<LessonResponse> lessons = lessonService.getLessonsByCourseId(courseId);
        return ResponseEntity.ok(lessons);
    }
    @GetMapping("/progress/user/{userId}/course/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<Double> getCourseProgress(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {
        double progress = progressService.calculateCourseProgress(userId, courseId);
        return ResponseEntity.ok(progress);
    }
    @GetMapping("/course/{courseId}/user/{userId}/with-progress")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public ResponseEntity<List<LessonWithProgressResponse>> getLessonsWithProgress(
            @PathVariable Long courseId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(progressService.getLessonsWithProgress(userId, courseId));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long id,
            @RequestBody LessonRequest request
    ) {
        return ResponseEntity.ok(lessonService.updateLesson(id, request));
    }
    @GetMapping("/course/{courseId}/user/{userId}/is-complete")
    public boolean hasUserCompletedCourse(@PathVariable Long courseId, @PathVariable Long userId) {
        int totalLessons = lessonRepository.countByCourseId(courseId);
        int completed = lessonProgressRepository.countCompletedLessons(userId, courseId);
        return totalLessons > 0 && completed == totalLessons;
    }


    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @GetMapping("/progress/user/{userId}/percentage")
    public ResponseEntity<Integer> getUserProgressPercentage(@PathVariable Long userId) {
        log.info("Calculating progress for user: {}", userId);
        try {
            int percentage = progressService.calculateOverallUserProgressPercentage(userId);
            return ResponseEntity.ok(percentage);
        } catch (Exception ex) {
            log.error("Error calculating progress for user: {}", userId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Integer> countLessonsByCourse(@PathVariable Long courseId) {
        int count = lessonRepository.countByCourseId(courseId);
        return ResponseEntity.ok(count);
    }



}
