package com.example.controller;

import com.example.service.OrderService;
import com.example.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/payment-status")
    public ResponseEntity<?> handlePaymentStatusUpdate(@RequestBody Map<String, Object> payload) {
        try {
            Long orderId = ((Number) payload.get("orderId")).longValue();
            String paymentStatus = (String) payload.get("status");
            
            logger.info("Received payment status update for order {}: {}", orderId, paymentStatus);
            
            Order.OrderStatus newOrderStatus = mapPaymentStatusToOrderStatus(paymentStatus);
            if (newOrderStatus != null) {
                orderService.updateOrderStatus(orderId, newOrderStatus);
                logger.info("Updated order {} status to {}", orderId, newOrderStatus);
            }
            
            return ResponseEntity.ok().body(Map.of("message", "Payment status updated successfully"));
            
        } catch (Exception e) {
            logger.error("Failed to process payment status update", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process webhook"));
        }
    }
    
    private Order.OrderStatus mapPaymentStatusToOrderStatus(String paymentStatus) {
        switch (paymentStatus.toUpperCase()) {
            case "COMPLETED":
                return Order.OrderStatus.CONFIRMED;
            case "FAILED":
            case "CANCELLED":
                return Order.OrderStatus.CANCELLED;
            default:
                return null; // No status change needed
        }
    }
}