package com.example.contentservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonWithProgressResponse {
    private Long lessonId;
    private String title;
    private String content;
    private String materialUrl;
    private int order;
    private boolean isCompleted;
}
