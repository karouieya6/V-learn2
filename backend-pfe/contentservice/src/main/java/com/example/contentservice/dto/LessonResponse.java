package com.example.contentservice.dto;

public class LessonResponse {

    private Long id;
    private String title;
    private String content;
    private Long courseId;
    private int order;
    private String materialUrl;

    // Constructor
    public LessonResponse(Long id, String title, String content, Long courseId, int order,String materialUrl  ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.order = order;
        this.materialUrl = materialUrl;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
    public String getMaterialUrl()      { return materialUrl; }
    public void setMaterialUrl(String u){ this.materialUrl = u; }
}
