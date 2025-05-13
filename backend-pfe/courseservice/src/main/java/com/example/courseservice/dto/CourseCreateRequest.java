package com.example.courseservice.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CourseCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String status;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
