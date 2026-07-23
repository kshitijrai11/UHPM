package com.ultrahpm.orderservice.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the order placement SAGA across multiple microservices.
 *
 * Steps:
 *   1. Reserve Inventory (Product Service via gRPC)
 *   2. Process Payment  (Payment Service via REST/gRPC)
 *   3. Confirm Order     (Local database update)
 *
 * If any step fails, all previously completed steps are compensated in reverse order.
 *
 * Per Docs V2 §7: Uses the SAGA pattern for eventual consistency, avoiding
 * the performance bottlenecks of traditional two-phase commits (2PC).
 */
@Component
public class OrderSaga {

    private static final Logger log = LoggerFactory.getLogger(OrderSaga.class);

    private final List<SagaStep<OrderSagaContext>> steps;

    public OrderSaga(
            ReserveInventoryStep reserveInventoryStep,
            ProcessPaymentStep processPaymentStep,
            ConfirmOrderStep confirmOrderStep
    ) {
        this.steps = List.of(reserveInventoryStep, processPaymentStep, confirmOrderStep);
    }

    /**
     * Execute the full SAGA. On failure at any step, compensate all completed steps
     * in reverse order.
     */
    public OrderSagaContext execute(OrderSagaContext context) {
        String sagaId = UUID.randomUUID().toString();
        context.setSagaId(sagaId);
        log.info("[SAGA {}] Starting order saga for userId={}", sagaId, context.getUserId());

        List<SagaStep<OrderSagaContext>> completedSteps = new ArrayList<>();

        for (SagaStep<OrderSagaContext> step : steps) {
            try {
                log.info("[SAGA {}] Executing step: {}", sagaId, step.getName());
                context = step.execute(context);
                completedSteps.add(step);
                log.info("[SAGA {}] Step completed: {}", sagaId, step.getName());
            } catch (Exception e) {
                log.error("[SAGA {}] Step FAILED: {}. Reason: {}", sagaId, step.getName(), e.getMessage());
                context.setFailed(true);
                context.setFailureReason(e.getMessage());

                // Compensate in reverse order
                Collections.reverse(completedSteps);
                for (SagaStep<OrderSagaContext> completedStep : completedSteps) {
                    try {
                        log.warn("[SAGA {}] Compensating step: {}", sagaId, completedStep.getName());
                        context = completedStep.compensate(context);
                    } catch (Exception compensationError) {
                        log.error("[SAGA {}] COMPENSATION FAILED for step: {}. MANUAL INTERVENTION REQUIRED.",
                                sagaId, completedStep.getName(), compensationError);
                    }
                }
                break;
            }
        }

        if (!context.isFailed()) {
            log.info("[SAGA {}] Order saga completed successfully. orderId={}", sagaId, context.getOrderId());
        }
        return context;
    }
}
