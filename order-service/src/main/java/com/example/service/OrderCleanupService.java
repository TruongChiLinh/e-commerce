package com.example.service;

import com.example.entity.Order;
import com.example.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(OrderCleanupService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cancelExpiredOrders() {
        logger.info("Starting expired orders cleanup job");
        
        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findByStatusAndExpiresAtBefore(
            Order.OrderStatus.PENDING, now);
        
        int cancelledCount = 0;
        for (Order order : expiredOrders) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            cancelledCount++;
            
            logger.info("Cancelled expired order: {} (created: {}, expired: {})", 
                order.getOrderNumber(), order.getCreatedAt(), order.getExpiresAt());
        }
        
        if (cancelledCount > 0) {
            logger.info("Cancelled {} expired orders", cancelledCount);
        }
    }
}