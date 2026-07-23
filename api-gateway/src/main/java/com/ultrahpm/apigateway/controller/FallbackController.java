package com.ultrahpm.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse(
                        "Product Service Unavailable",
                        "The product service is currently experiencing issues. Please try again later.",
                        "PRODUCT_SERVICE_DOWN"
                )));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse(
                        "Order Service Unavailable",
                        "The order service is currently experiencing issues. Please try again later.",
                        "ORDER_SERVICE_DOWN"
                )));
    }

    @GetMapping("/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse(
                        "Payment Service Unavailable",
                        "The payment service is currently experiencing issues. Please try again later.",
                        "PAYMENT_SERVICE_DOWN"
                )));
    }

    @GetMapping("/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFallbackResponse(
                        "Notification Service Unavailable",
                        "The notification service is currently experiencing issues. Please try again later.",
                        "NOTIFICATION_SERVICE_DOWN"
                )));
    }

    private Map<String, Object> createFallbackResponse(String error, String message, String errorCode) {
        return Map.of(
                "success", false,
                "error", error,
                "message", message,
                "errorCode", errorCode,
                "timestamp", LocalDateTime.now(),
                "fallback", true
        );
    }
}
