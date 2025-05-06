package com.example.contentservice.dto;

public class LessonProgressRequest {

    private Long userId;
    private Long lessonId;
    private boolean completed;

    // Constructors
    public LessonProgressRequest() {}

    public LessonProgressRequest(Long userId, Long lessonId, boolean completed) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.completed = completed;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
