package com.example.userservice.controller;

import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.model.AppUser;
import com.example.userservice.model.VerificationToken;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationTokenRepository;
import com.example.userservice.service.UserService;
import com.example.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.userservice.service.EmailService;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            AppUser user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ❌ Check if account is not verified
            if (!user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "❌ Please verify your email first"));
            }

            String token = jwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "username", user.getUsername()
            ));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }



    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);  // Remove "Bearer " prefix
        }

        userService.logout(token);
        return ResponseEntity.ok("✅ Logout successful!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "❌ Email is required!");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "❌ User not found!"));

        // ✅ Generate a secure random reset token
        String resetToken = UUID.randomUUID().toString();

        // ✅ Save the reset token associated with this email
        resetTokens.put(resetToken, email);

        // ✅ Build reset password link
        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

        // ✅ Prepare email content
        String emailBody = "Hello " + user.getFirstName() + ",\n\n"
                + "You requested a password reset. Click the link below to reset your password:\n"
                + resetLink + "\n\n"
                + "If you did not request this, ignore this email.\n\n"
                + "Best regards,\n"
                + "Your V-Learn Team";

        // ✅ Send email
        emailService.sendEmail(email, "Password Reset Request", emailBody);

        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Password reset link sent to your email!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "❌ Token and new password are required!");
        }

        if (!resetTokens.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "❌ Invalid or expired token!");
        }

        String email = resetTokens.get(token);

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "❌ User not found!"));

        // ✅ Update and encode the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // ✅ Remove the token (used only once)
        resetTokens.remove(token);

        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Password updated successfully!"));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AppUser user = new AppUser();
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(Set.of("STUDENT"));
            user.setEnabled(false); // not verified yet

            userRepository.save(user);

            // Generate token
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(token);
            verificationToken.setUser(user);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // token valid for 24h

            verificationTokenRepository.save(verificationToken);

            // Send email
            String link = "http://localhost:8080/userservice/auth/verify?token=" + token;
            String body = "Welcome " + user.getFirstName() + ",<br><br>" +
                    "Please verify your account by clicking the link below:<br>" +
                    "<a href=\"" + link + "\">Verify Account</a><br><br>" +
                    "Thanks,<br>The V-Learn Team";

            emailService.sendEmail(user.getEmail(), "Verify your V-Learn account", body);

            return ResponseEntity.ok(Map.of("message", "✅ Registration successful! Please check your email to verify your account."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Registration failed: " + e.getMessage()));
        }
    }
    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "❌ Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ Token expired"));
        }

        AppUser user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Optionally delete token
        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.ok(Map.of("message", "✅ Account verified successfully! You can now log in."));
    }





}
