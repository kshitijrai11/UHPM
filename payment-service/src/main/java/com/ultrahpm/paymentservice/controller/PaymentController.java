package com.ultrahpm.paymentservice.controller;

import com.ultrahpm.paymentservice.entity.Payment;
import com.ultrahpm.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody ProcessPaymentRequest request) {
        Payment payment = paymentService.processPayment(
                request.orderId(), request.userId(), request.amount(),
                request.gateway(), request.sagaId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refund(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refund(paymentId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    public record ProcessPaymentRequest(
            Long orderId, Long userId, BigDecimal amount, String gateway, String sagaId
    ) {}
}
