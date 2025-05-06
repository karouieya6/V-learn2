package com.example.courseservice.service;

import com.example.courseservice.model.CourseComment;
import com.example.courseservice.repository.CourseCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCommentService {

    private final CourseCommentRepository repository;

    public void addComment(Long courseId, Long studentId, String commentText) {
        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setStudentId(studentId); // ✅ Injected securely
        comment.setComment(commentText);
        comment.setCreatedAt(LocalDateTime.now());
        repository.save(comment);
    }


    public List<CourseComment> getCommentsByCourse(Long courseId) {
        return repository.findByCourseId(courseId);
    }

    public CourseComment replyToComment(Long commentId, String reply) {
        CourseComment comment = repository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setInstructorReply(reply);
        return repository.save(comment);
    }
}
