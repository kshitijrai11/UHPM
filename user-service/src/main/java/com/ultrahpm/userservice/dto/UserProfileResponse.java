package com.ultrahpm.userservice.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String username,
        String firstName,
        String lastName,
        String phone,
        String role,
        LocalDateTime createdAt
) {}
