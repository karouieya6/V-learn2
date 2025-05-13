package com.example.enrollmentservice.config;

import com.example.enrollmentservice.service.TokenBlacklistService;
import com.example.enrollmentservice.util.JwtUtil;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

        // ✅ Extract data from token
        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();
        ArrayList<String> roles = claims.get("roles", ArrayList.class);

        if (username == null || roles == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token payload");
            return;
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        User userDetails = new User(username, "", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // ✅ Set the authenticated user in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ✅ Debug logs
        System.out.println("🧠 Authenticated: " + username);
        System.out.println("🛡 Role: ROLE_" + roles);
        System.out.println("✅ Authorities: " + authentication.getAuthorities());

        filterChain.doFilter(request, response);
    }
}
