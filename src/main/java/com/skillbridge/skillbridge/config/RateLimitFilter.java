package com.skillbridge.skillbridge.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.skillbridge.skillbridge.service.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final int generalLimitPerMinute;
    private final int sensitiveLimitPerMinute;

    public RateLimitFilter(
            RateLimitService rateLimitService,
            @Value("${app.security.rate-limit.general-per-minute}") int generalLimitPerMinute,
            @Value("${app.security.rate-limit.sensitive-per-minute}") int sensitiveLimitPerMinute) {
        this.rateLimitService = rateLimitService;
        this.generalLimitPerMinute = generalLimitPerMinute;
        this.sensitiveLimitPerMinute = sensitiveLimitPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = isSensitivePath(path) ? sensitiveLimitPerMinute : generalLimitPerMinute;
        String clientKey = request.getRemoteAddr() + "|" + path;

        if (!rateLimitService.allowRequest(clientKey, limit)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again in a minute.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSensitivePath(String path) {
        return path.startsWith("/requests")
                || path.startsWith("/admin")
                || path.startsWith("/users");
    }
}