package com.example.courseservice.controller;

import com.example.courseservice.model.CourseComment;
import com.example.courseservice.service.CourseCommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.example.courseservice.dto.CommentRequest;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CourseCommentController {

    private final CourseCommentService service;



    @GetMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<CourseComment>> getComments(@PathVariable Long courseId) {
        return ResponseEntity.ok(service.getCommentsByCourse(courseId));
    }


    @PutMapping("/{commentId}/reply")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseComment> replyToComment(
            @PathVariable Long commentId,
            @RequestParam String reply
    ) {
        return ResponseEntity.ok(service.replyToComment(commentId, reply));
    }
    // ✅ KEEP THIS VERSION ONLY
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> addComment(
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long studentId = fetchUserIdFromUserService(httpRequest); // Secure from JWT
        service.addComment(request.getCourseId(), studentId, request.getComment());
        return ResponseEntity.ok("✅ Comment added");
    }
    private Long fetchUserIdFromUserService(HttpServletRequest request) {
        String url = "http://localhost:8080/userservice/user/email"; // ✅ your working endpoint
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Missing token");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Long> response = new RestTemplate().exchange(
                url,
                HttpMethod.GET,
                entity,
                Long.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Unauthorized or user ID not found");
        }

        return response.getBody();
    }


}
