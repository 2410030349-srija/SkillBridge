package com.skillbridge.skillbridge.dto;

public record AuthResponse(
        String token,
        String email,
        String role,
        Long userId
) {
}
