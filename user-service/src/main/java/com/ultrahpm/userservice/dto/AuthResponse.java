package com.ultrahpm.userservice.dto;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String email,
        String role
) {
    public AuthResponse(String token, Long userId, String email, String role) {
        this(token, "Bearer", userId, email, role);
    }
}
