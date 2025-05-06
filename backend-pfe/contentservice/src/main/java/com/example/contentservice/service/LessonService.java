package com.example.contentservice.service;

import com.example.contentservice.dto.LessonRequest;
import com.example.contentservice.dto.LessonResponse;
import com.example.contentservice.model.Lesson;
import com.example.contentservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonResponse createLesson(LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setCourseId(request.getCourseId());
        lesson.setLessonOrder(request.getOrder());

        Lesson saved = lessonRepository.save(lesson);
        return mapToResponse(saved);
    }

    public List<LessonResponse> getAllLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonById(Long id) {
        return lessonRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getCourseId(),
                lesson.getLessonOrder(),
                lesson.getMaterialUrl()
        );
    }
    public List<LessonResponse> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(lesson -> new LessonResponse(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getContent(),
                        lesson.getCourseId(),
                        lesson.getLessonOrder(),
                        lesson.getMaterialUrl()
                ))
                .collect(Collectors.toList());
    }
    public List<LessonResponse> getLessonsByCourseId(Long courseId) {
        return lessonRepository
                .findByCourseIdOrderByLessonOrderAsc(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }
    public String uploadMaterial(Long lessonId, MultipartFile file) {
        try {
            Path uploadPath = Paths.get("uploads/materials/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));

            lesson.setMaterialUrl("/uploads/materials/" + filename);
            lessonRepository.save(lesson);

            return "/uploads/materials/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload material", e);
        }
    }
    public LessonResponse updateLesson(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setMaterialUrl(request.getMaterialUrl());
        lesson.setLessonOrder(request.getOrder());

        Lesson updated = lessonRepository.save(lesson);

        return new LessonResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getContent(),
                updated.getCourseId(),
                updated.getLessonOrder(),
                updated.getMaterialUrl()
        );
    }

}
