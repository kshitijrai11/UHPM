package com.ultrahpm.notificationservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for order and payment events.
 * Processes Kafka streams to handle asynchronous notifications (email, SMS, push).
 *
 * Per Docs V2 §4: notification-service uses Virtual Threads (Loom) because
 * sending emails/SMS involves blocking I/O (SMTP, HTTP gateway calls).
 *
 * Per Docs V2 §3: Payloads are serialized using Avro and validated against
 * the Confluent Schema Registry.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Listens to order events and sends appropriate notifications.
     * In production, the Avro deserializer will automatically validate
     * the payload against the Schema Registry.
     */
    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvent(String payload) {
        log.info("Received order event: {}", payload);

        // Deserialize JSON payload and delegate to notification service
        notificationService.sendOrderNotification(payload);
    }

    /**
     * Listens to payment events and sends payment confirmation/failure notifications.
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvent(String payload) {
        log.info("Received payment event: {}", payload);

        notificationService.sendPaymentNotification(payload);
    }
}
