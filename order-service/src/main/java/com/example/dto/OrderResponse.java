package com.example.dto;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItem> orderItems;
    private LocalDateTime createdAt;

    // Constructors
    public OrderResponse() {}

    public OrderResponse(Order order, String customerName, String customerEmail) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.userId = order.getUserId();
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.shippingAddress = order.getShippingAddress();
        this.paymentMethod = order.getPaymentMethod();
        this.orderItems = order.getOrderItems();
        this.createdAt = order.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}