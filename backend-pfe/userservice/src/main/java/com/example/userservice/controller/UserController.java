package com.example.userservice.controller;

import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ✅ Get Logged-in User Profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT') or hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String email = authentication.getName();
        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        AppUser user = userOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("imageUrl", user.getImageUrl());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phone", user.getPhone());
        response.put("roles", user.getRoles());

        return ResponseEntity.ok(response);
    }


    /**
     * ✅ Update profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> updateUserProfile(Authentication authentication, @RequestBody Map<String, String> updates) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (updates.containsKey("username")) user.setUsername(updates.get("username"));
        if (updates.containsKey("email")) user.setEmail(updates.get("email"));
        if (updates.containsKey("firstName")) user.setFirstName(updates.get("firstName"));
        if (updates.containsKey("lastName")) user.setLastName(updates.get("lastName"));
        if (updates.containsKey("phone")) user.setPhone(updates.get("phone"));

        userRepository.save(user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Profile updated successfully!");
        return ResponseEntity.ok(user);
    }

    /**
     * ✅ Change password
     */
    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @RequestBody Map<String, String> passwords) {
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("❌ Missing required fields");
        }

        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Incorrect old password!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("✅ Password updated successfully!");
    }


    /**
     * ✅ Upload profile picture
     */
    @PostMapping("/{userId}/upload-profile")
    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = userService.saveProfilePicture(userId, file);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * ✅ Get All Users (Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        List<Map<String, Object>> sanitizedUsers = users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("roles", user.getRoles());
            return userData;
        }).toList();
        return ResponseEntity.ok(sanitizedUsers);
    }

    /**
     * ✅ Get User by ID (Admin only)
     */
    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ✅ Update any user (Admin only)
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUser(@PathVariable Long id,
                                             @RequestBody AppUser updatedUser,
                                             Authentication authentication) {
        AppUser adminUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!adminUser.getRoles().contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setRoles(updatedUser.getRoles());

        userRepository.save(user);
        return ResponseEntity.ok("✅ User updated successfully!");
    }

    /**
     * ✅ Delete a user (Admin only)
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, Authentication authentication) {
        String adminEmail = authentication.getName();
        userService.deleteUser(id, adminEmail);
        return ResponseEntity.ok("User deleted successfully!");
    }

    /**
     * ✅ Request instructor role (Students only)
     */
    @PostMapping("/request-instructor")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<?> requestInstructorRole(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Insert the instructor request with user_id and the current timestamp
        jdbcTemplate.update("INSERT INTO instructor_requests (user_id, created_at) VALUES (?, CURRENT_TIMESTAMP)", user.getId());

        return ResponseEntity.ok(Map.of("message", "✅ Request submitted for instructor role."));
    }


    /**
     * ✅ Approve instructor (Admin only)
     */
    @PutMapping("/approve-instructor/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> approveInstructor(@PathVariable Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().add("INSTRUCTOR");
        user.setForceReLogin(true);
        userRepository.save(user);

        // ✅ Delete the request from instructor_requests after promotion
        jdbcTemplate.update("DELETE FROM instructor_requests WHERE user_id = ?", userId);

        return ResponseEntity.ok(Map.of("message", "✅ User promoted to INSTRUCTOR and request deleted"));
    }


    /**
     * ✅ Get instructor requests (Admin only)
     */
    @GetMapping("/instructor-requests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getInstructorRequests() {
        String sql = """
            SELECT u.id, u.email, u.username, ir.status
            FROM instructor_requests ir
            JOIN users u ON u.id = ir.user_id
            WHERE ir.status = 'PENDING'
        """;
        List<Map<String, Object>> requests = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(requests);
    }

    /**
     * ✅ Get user ID by email (Instructors only)
     */
    @GetMapping("/email")
    public ResponseEntity<Long> getUserIdFromToken(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long userId = userService.getUserIdByEmail(email);
        return ResponseEntity.ok(userId);
    }
    @GetMapping("/files/profile/{filename:.+}")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get("uploads/profiles").resolve(filename);
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }



}