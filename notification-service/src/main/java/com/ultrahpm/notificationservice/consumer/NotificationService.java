package com.ultrahpm.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ultrahpm.notificationservice.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Handles sending notifications via email.
 * Uses Virtual Threads since SMTP calls are inherently blocking.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public NotificationService(JavaMailSender mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    /**
     * Send an order confirmation email.
     */
    public void sendOrderNotification(String orderEventPayload) {
        try {
            OrderEvent event = objectMapper.readValue(orderEventPayload, OrderEvent.class);
            
            // Note: In a real system, we'd look up the user's email from user-service
            // For now, sending to a dummy recipient
            String recipient = "customer_" + event.userId() + "@example.com";
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("orders@ultrahpm.com");
            message.setTo(recipient);
            message.setSubject("Order Confirmation - " + event.orderId());
            message.setText("Thank you for your order!\n\n" +
                    "Order ID: " + event.orderId() + "\n" +
                    "Total Amount: $" + event.totalAmount() + "\n" +
                    "Status: " + event.status() + "\n\n" +
                    "Shipping to: " + event.shippingAddress() + "\n\n" +
                    "We will notify you once it ships.");

            mailSender.send(message);
            log.info("📧 Sent order confirmation email for orderId={} to {}", event.orderId(), recipient);

        } catch (Exception e) {
            log.error("Failed to process or send order notification", e);
        }
    }

    /**
     * Send a payment-related notification (success, failure, refund).
     */
    public void sendPaymentNotification(String paymentEventPayload) {
        log.info("💳 Payment notification received (logging only for now): {}", paymentEventPayload);
    }
}

