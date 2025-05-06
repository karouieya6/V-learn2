package com.example.contentservice;

import com.example.contentservice.dto.LessonRequest;
import com.example.contentservice.dto.LessonResponse;
import com.example.contentservice.model.Lesson;
import com.example.contentservice.repository.LessonRepository;
import com.example.contentservice.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LessonServiceTest {

    private LessonRepository lessonRepository;
    private LessonService lessonService;

    @BeforeEach
    void setUp() {
        lessonRepository = Mockito.mock(LessonRepository.class);
        lessonService = new LessonService(lessonRepository);
    }

    @Test
    void testCreateLesson() {
        LessonRequest request = LessonRequest.builder()
                .title("Test Lesson")
                .content("Some content")
                .courseId(1L)
                .order(1)
                .build();

        Lesson savedLesson = Lesson.builder()
                .id(1L)
                .title("Test Lesson")
                .content("Some content")
                .courseId(1L)
                .lessonOrder(1)

                .build();

        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);

        LessonResponse response = lessonService.createLesson(request);

        assertEquals("Test Lesson", response.getTitle());
        assertEquals("Some content", response.getContent());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void testGetAllLessons() {
        Lesson lesson = Lesson.builder()
                .id(1L)
                .title("L1")
                .content("C1")
                .courseId(1L)
                .lessonOrder(1)

                .build();

        when(lessonRepository.findAll()).thenReturn(List.of(lesson));

        List<LessonResponse> result = lessonService.getAllLessons();

        assertEquals(1, result.size());
        assertEquals("L1", result.get(0).getTitle());
    }
}
