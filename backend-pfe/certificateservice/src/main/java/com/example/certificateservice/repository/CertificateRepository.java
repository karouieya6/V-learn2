package com.example.certificateservice.repository;

import com.example.certificateservice.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserId(Long userId);
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

}
