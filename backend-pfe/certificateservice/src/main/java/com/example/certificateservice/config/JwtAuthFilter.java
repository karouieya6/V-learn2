package com.example.certificateservice.config;

import com.example.certificateservice.service.TokenBlacklistService;
import com.example.certificateservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        System.out.println("JwtAuthFilter triggered");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (tokenBlacklistService.isTokenRevoked(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is revoked");
            return;
        }

        if (!jwtUtil.isTokenValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();

        // Flexible extraction: handles both "roles" (list) and "role" (single)
        List<String> roles = new ArrayList<>();
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            for (Object role : (List<?>) rolesObj) {
                roles.add(role.toString());
            }
        } else if (rolesObj instanceof String) {
            roles.add(rolesObj.toString());
        } else {
            Object singleRole = claims.get("role"); // fallback to 'role' if 'roles' is missing
            if (singleRole != null) {
                roles.add(singleRole.toString());
            }
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        UserDetails userDetails = new User(username, "", authorities);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Debug logs
        System.out.println("TOKEN: " + token);
        System.out.println("Roles claim: " + roles);
        System.out.println("Authorities: " + authorities);

        filterChain.doFilter(request, response);
    }
}
