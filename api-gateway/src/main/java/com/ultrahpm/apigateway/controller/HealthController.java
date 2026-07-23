package com.ultrahpm.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        )));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "name", "UltraHPM API Gateway",
                "description", "High-Performance Reactive API Gateway",
                "version", "1.0.0",
                "profiles", System.getProperty("spring.profiles.active", "default"),
                "timestamp", LocalDateTime.now()
        )));
    }
}
