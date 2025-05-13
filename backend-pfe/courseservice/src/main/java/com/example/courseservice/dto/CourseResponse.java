package com.example.courseservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String instructorName;
    private String status;
    private String categoryName;
    private Long categoryId; // ✅ Add this line
}
