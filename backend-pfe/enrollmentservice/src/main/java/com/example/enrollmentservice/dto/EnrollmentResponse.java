package com.example.enrollmentservice.dto;

import com.example.enrollmentservice.model.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // ✅ Required for JSON deserialization and builder usage in tests
public class EnrollmentResponse {
    private Long id;
    private Long userId;
    private Long courseId;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
}
