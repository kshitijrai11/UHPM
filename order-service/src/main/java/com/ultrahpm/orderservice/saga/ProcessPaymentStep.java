package com.ultrahpm.orderservice.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * SAGA Step 2: Process payment via the Payment Service.
 * Compensation: Initiate a refund.
 *
 * Uses RestClient (Java 21 / Spring 6.1) for inter-service communication.
 */
@Component
public class ProcessPaymentStep implements SagaStep<OrderSagaContext> {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentStep.class);

    private final RestClient restClient;

    public ProcessPaymentStep() {
        // TODO: Replace with discovery-aware URL when Eureka is running
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    @Override
    public OrderSagaContext execute(OrderSagaContext context) throws Exception {
        log.info("Processing payment of {} for userId={} via Payment Service...",
                context.getTotalAmount(), context.getUserId());

        Map<?, ?> response = restClient.post()
                .uri("/api/v1/payments")
                .body(Map.of(
                        "orderId", context.getOrderId() != null ? context.getOrderId() : 0,
                        "userId", context.getUserId(),
                        "amount", context.getTotalAmount(),
                        "gateway", "INTERNAL",
                        "sagaId", context.getSagaId()
                ))
                .retrieve()
                .body(Map.class);

        if (response != null) {
            Number paymentId = (Number) response.get("id");
            String txnRef = (String) response.get("transactionRef");
            context.setPaymentId(paymentId != null ? paymentId.longValue() : null);
            context.setPaymentTransactionRef(txnRef);
            log.info("Payment processed. paymentId={}, txnRef={}", context.getPaymentId(), txnRef);
        }

        return context;
    }

    @Override
    public OrderSagaContext compensate(OrderSagaContext context) {
        log.warn("COMPENSATING: Initiating refund for paymentId={}", context.getPaymentId());

        if (context.getPaymentId() != null) {
            try {
                restClient.post()
                        .uri("/api/v1/payments/{id}/refund", context.getPaymentId())
                        .retrieve()
                        .body(Map.class);
                log.info("Refund initiated for paymentId={}", context.getPaymentId());
            } catch (Exception e) {
                log.error("Refund failed for paymentId={}. MANUAL INTERVENTION REQUIRED.", context.getPaymentId(), e);
            }
        }

        return context;
    }

    @Override
    public String getName() {
        return "ProcessPayment";
    }
}
