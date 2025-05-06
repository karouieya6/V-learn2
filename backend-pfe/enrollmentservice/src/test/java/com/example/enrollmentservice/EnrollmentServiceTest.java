package com.example.enrollmentservice;

import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.model.Enrollment;
import com.example.enrollmentservice.model.EnrollmentStatus;
import com.example.enrollmentservice.repository.EnrollmentRepository;
import com.example.enrollmentservice.service.EnrollmentService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnrollUser_successfulEnrollment() {
        EnrollmentRequest request = new EnrollmentRequest(2L); // only courseId

        // Simulate userId from user service
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer test-token");
        when(restTemplate.exchange(
                eq("http://userservice/api/users/email"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Long.class)
        )).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());

        Enrollment saved = new Enrollment();
        saved.setUserId(1L);
        saved.setCourseId(2L);
        saved.setEnrolledAt(LocalDateTime.now());
        saved.setStatus(EnrollmentStatus.ENROLLED);

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);

        EnrollmentResponse result = enrollmentService.enrollUser(request, httpRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getCourseId());
    }

    @Test
    void testEnrollUser_alreadyEnrolled_shouldThrowException() {
        EnrollmentRequest request = new EnrollmentRequest(2L);

        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer token");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Long.class)))
                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        Enrollment existing = new Enrollment();
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> enrollmentService.enrollUser(request, httpRequest));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void testIsUserEnrolled_true() {
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(new Enrollment()));
        boolean enrolled = enrollmentService.isUserEnrolled(1L, 2L);
        assertTrue(enrolled);
    }

    @Test
    void testIsUserEnrolled_false() {
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        boolean enrolled = enrollmentService.isUserEnrolled(1L, 2L);
        assertFalse(enrolled);
    }

    @Test
    void testGetAllEnrollments() {
        Enrollment e1 = new Enrollment();
        e1.setUserId(1L);
        e1.setCourseId(2L);
        e1.setStatus(EnrollmentStatus.ENROLLED);

        when(enrollmentRepository.findAll()).thenReturn(List.of(e1));

        List<EnrollmentResponse> result = enrollmentService.getAllEnrollments();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getCourseId());
    }
}
