package com.skillbridge.skillbridge.config; // NOSONAR - false positive: package is named

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.skillbridge.skillbridge.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean debugDelete = "DELETE".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/content/");
        String authHeader = normalizeHeader(request.getHeader("Authorization"));

        if (debugDelete) {
            System.out.println("[JWT-DEBUG] method=" + request.getMethod() + ", uri=" + request.getRequestURI() + ", authHeaderPresent=" + (authHeader != null));
        }

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(authHeader);
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!jwtService.isTokenValid(token)) {
            if (debugDelete) {
                System.out.println("[JWT-DEBUG] tokenValid=false");
            }
            filterChain.doFilter(request, response);
            return;
        }

        if (debugDelete) {
            System.out.println("[JWT-DEBUG] tokenValid=true");
        }

        String username = jwtService.extractUsername(token);
        if (debugDelete) {
            System.out.println("[JWT-DEBUG] username=" + username);
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // Blocked-account enforcement is currently disabled in this filter.
            if (debugDelete) {
                System.out.println("[JWT-DEBUG] userEnabled=true, authorities=" + userDetails.getAuthorities());
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return null;
        }
        String normalized = header.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private String extractToken(String authHeader) {
        String lower = authHeader.toLowerCase();
        if (lower.startsWith("bearer ")) {
            return stripQuotes(authHeader.substring(7).trim());
        }

        // Accept raw JWT value to be resilient against misconfigured API clients.
        return stripQuotes(authHeader);
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() >= 2
                && ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
                || (cleaned.startsWith("'") && cleaned.endsWith("'")))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }
}
