package com.ultrahpm.orderservice.service;

import com.ultrahpm.orderservice.entity.Order;
import com.ultrahpm.orderservice.repository.OrderRepository;
import com.ultrahpm.orderservice.saga.OrderSaga;
import com.ultrahpm.orderservice.saga.OrderSagaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderSaga orderSaga;
    private final OrderValidator orderValidator;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, OrderSaga orderSaga, OrderValidator orderValidator,
                        org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.orderSaga = orderSaga;
        this.orderValidator = orderValidator;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Place a new order by executing the SAGA orchestrator.
     */
    public OrderSagaContext placeOrder(Long userId, List<OrderSagaContext.OrderItemRequest> items, String shippingAddress) {
        log.info("Initiating pre-checkout validation using Structured Concurrency for userId={}", userId);
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork independent validation tasks to run concurrently on virtual threads
            StructuredTaskScope.Subtask<Boolean> userSubtask = scope.fork(() -> orderValidator.verifyUserAccount(userId));
            StructuredTaskScope.Subtask<Boolean> fraudSubtask = scope.fork(() -> orderValidator.checkFraudRisk(userId));
            StructuredTaskScope.Subtask<Boolean> addressSubtask = scope.fork(() -> orderValidator.validateAddress(shippingAddress));

            // Wait for all tasks to complete or the first one to fail
            scope.join();
            
            // If any task threw an exception, throw it here (canceling others if they were still running)
            scope.throwIfFailed();
            
            log.info("Pre-checkout validation successful for userId={}", userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Order placement interrupted during validation", e);
        } catch (Exception e) {
            log.error("Pre-checkout validation failed: {}", e.getMessage());
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }

        // Calculate total
        BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderSagaContext context = new OrderSagaContext(userId, items, shippingAddress);
        context.setTotalAmount(total);

        OrderSagaContext result = orderSaga.execute(context);
        
        // If SAGA completes successfully, publish event for Notification Service
        if (result.getOrderId() != null) {
            publishOrderEvent(result);
        }
        
        return result;
    }

    private void publishOrderEvent(OrderSagaContext context) {
        try {
            com.ultrahpm.orderservice.dto.OrderEvent event = new com.ultrahpm.orderservice.dto.OrderEvent(
                    context.getOrderId(),
                    context.getUserId(),
                    context.getTotalAmount(),
                    "PAID",
                    context.getShippingAddress(),
                    context.getItems().stream().map(i -> new com.ultrahpm.orderservice.dto.OrderEvent.OrderItemEvent(
                            i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()
                    )).toList()
            );
            
            log.info("Publishing order event to Kafka for orderId={}", context.getOrderId());
            kafkaTemplate.send("order-events", String.valueOf(context.getOrderId()), event);
        } catch (Exception e) {
            log.error("Failed to publish order event for orderId={}", context.getOrderId(), e);
        }
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
