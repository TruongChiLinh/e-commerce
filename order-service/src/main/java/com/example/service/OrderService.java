package com.example.service;

import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.repository.OrderRepository;
import com.example.dto.CreateOrderRequest;
import com.example.dto.PlaceOrderRequest;
import com.example.dto.OrderResponse;
import com.example.common.security.JwtUtil;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String USER_SERVICE_URL = "http://localhost:8081";
    private static final String AUTH_SERVICE_URL = "http://localhost:8084";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8085";

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RestTemplate restTemplate;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order createOrder(Order order) {
        // Generate unique order number
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        // Set expiration time to 10 minutes from now
        order.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderDetails) {
        Order order = getOrderById(id);
        
        order.setShippingAddress(orderDetails.getShippingAddress());
        order.setPaymentMethod(orderDetails.getPaymentMethod());
        order.setTotalAmount(orderDetails.getTotalAmount());
        order.setUpdatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }

    public Long getOrderCountByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    public Order placeOrderWithPayment(CreateOrderRequest request) {
        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        
        // Calculate total amount and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTotalPrice(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        
        // Save order
        Order savedOrder = createOrder(order);
        
        // Create payment via REST call to payment service
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("orderId", savedOrder.getId());
            paymentRequest.put("userId", savedOrder.getUserId());
            paymentRequest.put("amount", savedOrder.getTotalAmount());
            paymentRequest.put("method", request.getPaymentMethod().toUpperCase());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
            
            // Call payment service (assuming it runs on port 8085)
            restTemplate.postForObject("http://localhost:8085/api/payments", entity, Object.class);
            
        } catch (Exception e) {
            // Log error but don't fail the order
            System.err.println("Failed to create payment: " + e.getMessage());
        }
        
        return savedOrder;
    }
    
    public OrderResponse placeOrderWithAuth(PlaceOrderRequest request, String token) {
        // Extract user info from JWT token
        String username = jwtUtil.getUsernameFromToken(token);
        Long userId = getUserIdByUsername(username); // You need to implement this
        
        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        
        // Calculate total amount and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (PlaceOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTotalPrice(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);
        
        // Save order
        Order savedOrder = createOrder(order);
        
        // Create payment via REST call to payment service
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("orderId", savedOrder.getId());
            paymentRequest.put("userId", savedOrder.getUserId());
            paymentRequest.put("amount", savedOrder.getTotalAmount());
            paymentRequest.put("method", request.getPaymentMethod().toUpperCase());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
            
            // Call payment service
            restTemplate.postForObject(PAYMENT_SERVICE_URL + "/api/payments/create-for-order", entity, Object.class);
            logger.info("Payment creation initiated for order: {}", savedOrder.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create payment for order: {}", savedOrder.getId(), e);
            // Cancel the order since payment creation failed
            savedOrder.setStatus(Order.OrderStatus.CANCELLED);
            savedOrder.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(savedOrder);
            logger.info("Order {} cancelled due to payment creation failure", savedOrder.getId());
        }
        
        // Get customer info and return response
        String customerName = getCustomerNameByUserId(userId);
        String customerEmail = getCustomerEmailByUserId(userId);
        
        return new OrderResponse(savedOrder, customerName, customerEmail);
    }
    
    private Long getUserIdByUsername(String username) {
        try {
            Map<String, Object> userInfo = restTemplate.getForObject(
                AUTH_SERVICE_URL + "/api/auth/user/" + username, Map.class);
            
            if (userInfo != null && userInfo.get("data") != null) {
                Map<String, Object> userData = (Map<String, Object>) userInfo.get("data");
                return userData != null ? ((Number) userData.get("id")).longValue() : null;
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to get user ID for username: {}", username, e);
            throw new BusinessException("Unable to retrieve user information");
        }
    }
    
    private String getCustomerNameByUserId(Long userId) {
        try {
            Map<String, Object> userInfo = restTemplate.getForObject(
                USER_SERVICE_URL + "/api/users/user/" + userId, Map.class);
            return userInfo != null ? (String) userInfo.get("fullName") : "Unknown Customer";
        } catch (Exception e) {
            return "Unknown Customer";
        }
    }
    
    private String getCustomerEmailByUserId(Long userId) {
        return "customer" + userId + "@example.com";
    }
}