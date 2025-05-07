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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        ArrayList<String> role = claims.get("roles", ArrayList.class);
        String roleName = role.get(0);

        if (username == null || role == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token payload");
            return;
        }

        // ✅ Convert role to authority
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

        User userDetails = new User(username, "", Collections.singletonList(authority));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // ✅ Set the authenticated user in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ✅ Debug logs
        System.out.println("🧠 Authenticated: " + username);
        System.out.println("🛡 Role: ROLE_" + role);
        System.out.println("✅ Authorities: " + authentication.getAuthorities());

        filterChain.doFilter(request, response);
    }
}
