package com.example.enrollmentservice.dto;

public record ApiResponse<T>(String status, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data);
    }

    public static <T> ApiResponse<T> fail(T data) {
        return new ApiResponse<>("fail", data);
    }
}
