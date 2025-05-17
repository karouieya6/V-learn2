package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "users")
public class AppUser implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    @Column(length = 20)
    private String phone;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;



    @Column(name = "profile_image_url")
    private String profileImageUrl;
    @Column(name = "force_relogin")
    private boolean forceReLogin = false;

    // Getter and Setter
    public boolean isForceReLogin() {
        return forceReLogin;
    }

    public void setForceReLogin(boolean forceReLogin) {
        this.forceReLogin = forceReLogin;
    }

    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private Boolean active = true;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // Optional "ROLE_" prefix for Spring Security
                .toList();
    }
    public String getImageUrl() {
        return profileImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }


    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

