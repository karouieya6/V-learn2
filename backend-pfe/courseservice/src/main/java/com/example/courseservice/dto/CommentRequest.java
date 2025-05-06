package com.example.courseservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Long courseId;
    private String comment;
}
