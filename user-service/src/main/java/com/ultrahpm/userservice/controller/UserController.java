package com.ultrahpm.userservice.controller;

import com.ultrahpm.userservice.dto.UserProfileResponse;
import com.ultrahpm.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the profile of the currently authenticated user.
     * The userId is extracted from the JWT token by the security filter.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    /**
     * Get a user profile by ID (for inter-service communication).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
