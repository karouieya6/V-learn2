package com.example.certificateservice.controller;

import com.example.certificateservice.dto.CertificateRequest;
import com.example.certificateservice.dto.CertificateResponse;
import com.example.certificateservice.repository.CertificateRepository;
import com.example.certificateservice.service.CertificateService;
import com.example.certificateservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {
    @Autowired
    private CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final JwtUtil jwtUtil;
    @GetMapping("/admin/stats/total-certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalCertificates() {
        return certificateRepository.count();
    }



    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CertificateResponse>> getCertificatesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(certificateService.getCertificatesByUser(userId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Long id) {
        return certificateService.downloadCertificate(id);
    }
    @GetMapping("/user/{userId}/count")
    public long countCertificatesByUser(@PathVariable Long userId) {
        return certificateRepository.countByUserId(userId);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> generateCertificate(
            @RequestBody CertificateRequest request,
            HttpServletRequest httpRequest
    ) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.extractUserId(token); // You must implement this

        return ResponseEntity.ok(certificateService.generateCertificate(request));
    }




}
