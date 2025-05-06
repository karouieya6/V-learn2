package com.example.certificateservice.service;

import com.example.certificateservice.dto.CertificateRequest;
import com.example.certificateservice.dto.CertificateResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CertificateService {
    CertificateResponse generateCertificate(CertificateRequest request);
    List<CertificateResponse> getCertificatesByUser(Long userId);
    ResponseEntity<Resource> downloadCertificate(Long id);
}
