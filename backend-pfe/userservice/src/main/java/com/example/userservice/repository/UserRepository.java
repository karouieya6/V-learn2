package com.example.userservice.repository;

import com.example.userservice.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM AppUser u JOIN u.roles r WHERE r.name <> :role")
    long countNonAdminUsers(@Param("role") String role);

    Optional<AppUser> findByUsername(String username);
    @Query("SELECT u FROM AppUser u JOIN u.roles r WHERE r.name = :role")
    List<AppUser> findAllByRole(@Param("role") String role);


    Page<AppUser> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    @Query("""
    SELECT u FROM AppUser u 
    JOIN u.roles r 
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) 
    AND r.name = :role
""")
    Page<AppUser> findByRoleAndUsernameContainingIgnoreCase(
            @Param("role") String role,
            @Param("search") String search,
            Pageable pageable
    );


    // Add this method to find active users
    List<AppUser> findByActiveTrue(); // Only return active users
}
