package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class CourseStatsResponse {
        private Long courseId;
        private String courseTitle;
        private Long enrollmentCount;
    }


