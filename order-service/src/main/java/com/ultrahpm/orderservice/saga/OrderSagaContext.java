package com.ultrahpm.orderservice.saga;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carries state across all SAGA steps during order processing.
 */
public class OrderSagaContext {

    private String sagaId;
    private Long userId;
    private Long orderId;
    private List<OrderItemRequest> items;
    private BigDecimal totalAmount;
    private String shippingAddress;

    // Payment tracking
    private Long paymentId;
    private String paymentTransactionRef;

    // Saga state
    private boolean failed = false;
    private String failureReason;

    // Constructors
    public OrderSagaContext() {}

    public OrderSagaContext(Long userId, List<OrderItemRequest> items, String shippingAddress) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
    }

    // Getters / Setters
    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getPaymentTransactionRef() { return paymentTransactionRef; }
    public void setPaymentTransactionRef(String ref) { this.paymentTransactionRef = ref; }
    public boolean isFailed() { return failed; }
    public void setFailed(boolean failed) { this.failed = failed; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    /**
     * Inner DTO for items in the saga request.
     */
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public OrderItemRequest() {}
        public OrderItemRequest(Long productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
