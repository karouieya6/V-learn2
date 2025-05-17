package com.example.contentservice.dto;

public class LessonProgressRequest {

    private Long userId;
    private Long lessonId;
    private boolean completed;
    private Long courseId;
    // Constructors
    public LessonProgressRequest() {}

    public LessonProgressRequest(Long userId, Long lessonId, boolean completed) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.courseId = courseId;
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
    public Long getCourseId() {          // <--- Add this getter
        return courseId;
    }

    public void setCourseId(Long courseId) { // <--- Add this setter
        this.courseId = courseId;
    }

}
