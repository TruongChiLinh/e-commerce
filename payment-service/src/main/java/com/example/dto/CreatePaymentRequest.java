package com.example.dto;

import java.math.BigDecimal;

public class CreatePaymentRequest {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String method;

    // Constructors
    public CreatePaymentRequest() {}

    public CreatePaymentRequest(Long orderId, Long userId, BigDecimal amount, String method) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}