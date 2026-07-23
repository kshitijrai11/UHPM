package com.ultrahpm.recommendationservice.event;

public class UserEvent {
    private String userId;
    private String eventType; // VIEW, ADD_TO_CART, PURCHASE
    private String productId;
    private long timestamp;

    public UserEvent() {}

    public UserEvent(String userId, String eventType, String productId, long timestamp) {
        this.userId = userId;
        this.eventType = eventType;
        this.productId = productId;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return "UserEvent{" +
                "userId='" + userId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", productId='" + productId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
