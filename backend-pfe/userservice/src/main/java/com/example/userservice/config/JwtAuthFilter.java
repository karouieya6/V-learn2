package com.example.userservice.config;

import com.example.userservice.service.TokenBlacklistService;
import com.example.userservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.model.AppUser;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Request path: " + request.getServletPath());
        String path = request.getServletPath();

// ✅ Bypass JWT filter for public endpoints (no userservice prefix inside microservice!)
        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }



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

        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();
        AppUser user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isForceReLogin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("❌ You must log in again due to a role change.");
            return;
        }


        ArrayList<String> roleList = claims.get("roles", ArrayList.class);

        if (username == null || roleList == null || roleList.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token payload");
            return;
        }

        List<GrantedAuthority> authorities = roleList.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .map(authority -> (GrantedAuthority) authority)
                .toList();

        User userDetails = new User(username, "", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
