package com.example.certificateservice.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private String userName;
    private LocalDate issueDate;
    private String filePath;
}
