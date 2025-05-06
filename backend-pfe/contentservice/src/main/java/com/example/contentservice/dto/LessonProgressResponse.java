package com.example.contentservice.dto;

public class LessonProgressResponse {

    private Long id;
    private Long userId;
    private Long lessonId;
    private boolean completed;

    // Constructor
    public LessonProgressResponse(Long id, Long userId, Long lessonId, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.lessonId = lessonId;
        this.completed = completed;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
