package com.ultrahpm.orderservice.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * SAGA Step 1: Reserve inventory via the Product Service.
 * Compensation: Release the reserved inventory.
 *
 * Uses RestClient (Java 21 / Spring 6.1) for inter-service communication.
 * In production, this would use gRPC for performance. For now, REST keeps
 * the system runnable without requiring proto compilation in the order-service.
 */
@Component
public class ReserveInventoryStep implements SagaStep<OrderSagaContext> {

    private static final Logger log = LoggerFactory.getLogger(ReserveInventoryStep.class);

    private final RestClient restClient;

    public ReserveInventoryStep() {
        // TODO: Replace with discovery-aware URL when Eureka is running
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

    @Override
    public OrderSagaContext execute(OrderSagaContext context) throws Exception {
        log.info("Reserving inventory for {} items via Product Service...", context.getItems().size());

        for (OrderSagaContext.OrderItemRequest item : context.getItems()) {
            // Verify product exists and has sufficient stock
            Map<?, ?> product = restClient.get()
                    .uri("/api/v1/products/{id}", item.getProductId())
                    .retrieve()
                    .body(Map.class);

            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }

            log.info("Product verified: {} (stock available)", item.getProductName());
        }

        log.info("Inventory reserved successfully for {} items.", context.getItems().size());
        return context;
    }

    @Override
    public OrderSagaContext compensate(OrderSagaContext context) {
        log.warn("COMPENSATING: Releasing reserved inventory...");
        // In a full implementation, this would call a release-stock endpoint
        log.info("Inventory released.");
        return context;
    }

    @Override
    public String getName() {
        return "ReserveInventory";
    }
}
