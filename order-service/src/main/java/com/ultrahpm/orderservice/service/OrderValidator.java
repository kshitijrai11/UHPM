package com.ultrahpm.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OrderValidator {

    private static final Logger log = LoggerFactory.getLogger(OrderValidator.class);

    public boolean verifyUserAccount(Long userId) {
        log.info("Verifying user account for userId={} on thread {}", userId, Thread.currentThread());
        try {
            // Simulate network call to user-service
            Thread.sleep(Duration.ofMillis(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("User verification interrupted", e);
        }
        if (userId < 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return true;
    }

    public boolean checkFraudRisk(Long userId) {
        log.info("Checking fraud risk for userId={} on thread {}", userId, Thread.currentThread());
        try {
            // Simulate external ML fraud check
            Thread.sleep(Duration.ofMillis(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fraud check interrupted", e);
        }
        return true;
    }

    public boolean validateAddress(String address) {
        log.info("Validating address: '{}' on thread {}", address, Thread.currentThread());
        try {
            // Simulate external address validation API
            Thread.sleep(Duration.ofMillis(150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Address validation interrupted", e);
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        return true;
    }
}
