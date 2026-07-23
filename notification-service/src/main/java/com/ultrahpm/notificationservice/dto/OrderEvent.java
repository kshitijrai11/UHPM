package com.ultrahpm.notificationservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderEvent(
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        String status,
        String shippingAddress,
        List<OrderItemEvent> items
) {
    public record OrderItemEvent(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}
