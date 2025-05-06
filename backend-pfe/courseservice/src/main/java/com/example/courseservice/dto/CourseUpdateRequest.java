package com.example.courseservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseUpdateRequest {
    private String title;
    private String description;
    private Long instructorId;
    private Long categoryId;
}
