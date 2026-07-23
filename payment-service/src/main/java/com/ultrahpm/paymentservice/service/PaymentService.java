package com.ultrahpm.paymentservice.service;

import com.ultrahpm.paymentservice.entity.Payment;
import com.ultrahpm.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process a payment for the given order.
     * In a real system, this would call an external payment gateway (Stripe, PayPal, etc.)
     */
    public Payment processPayment(Long orderId, Long userId, BigDecimal amount, String gateway, String sagaId) {
        log.info("Processing payment: orderId={}, amount={}, gateway={}", orderId, amount, gateway);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setGateway(gateway);
        payment.setSagaId(sagaId);
        payment.setStatus("PROCESSING");

        // Simulate gateway call
        payment.setTransactionRef("txn_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setStatus("COMPLETED");

        Payment saved = paymentRepository.save(payment);
        log.info("Payment completed: paymentId={}, txnRef={}", saved.getId(), saved.getTransactionRef());
        return saved;
    }

    /**
     * Refund a payment (SAGA compensation).
     */
    public Payment refund(Long paymentId) {
        log.warn("Initiating refund for paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }
}
