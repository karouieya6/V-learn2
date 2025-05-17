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
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
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

        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.getSubject();
        ArrayList<String> roles = claims.get("roles", ArrayList.class);

        // Support multiple roles!
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (String r : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
            }
        }

        UserDetails userDetails = new User(username, "", authorities);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Debug logs
        System.out.println("🧠 Authenticated: " + username);
        System.out.println("🛡 Roles: " + authorities);
        System.out.println("✅ Authorities: " + authentication.getAuthorities());

        filterChain.doFilter(request, response);
    }
}
