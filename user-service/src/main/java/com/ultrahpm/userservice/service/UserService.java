package com.ultrahpm.userservice.service;

import com.ultrahpm.userservice.dto.*;
import com.ultrahpm.userservice.entity.User;
import com.ultrahpm.userservice.repository.UserRepository;
import com.ultrahpm.userservice.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole("CUSTOMER");

        user = userRepository.save(user);
        log.info("User registered successfully: userId={}", user.getId());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.info("Login successful for userId={}", user.getId());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
