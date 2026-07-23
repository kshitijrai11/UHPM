package com.ultrahpm.orderservice.controller;

import com.ultrahpm.orderservice.entity.Order;
import com.ultrahpm.orderservice.saga.OrderSagaContext;
import com.ultrahpm.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Place a new order. Triggers the full SAGA orchestration.
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderSagaContext result = orderService.placeOrder(
                request.userId(),
                request.items(),
                request.shippingAddress()
        );

        if (result.isFailed()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of(
                            "status", "FAILED",
                            "sagaId", result.getSagaId(),
                            "reason", result.getFailureReason()
                    ));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "SUCCESS",
                        "orderId", result.getOrderId(),
                        "sagaId", result.getSagaId()
                ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    /**
     * Request DTO for placing an order.
     */
    public record PlaceOrderRequest(
            Long userId,
            List<OrderSagaContext.OrderItemRequest> items,
            String shippingAddress
    ) {}
}
