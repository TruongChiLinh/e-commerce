package com.example.controller;

import com.example.entity.Order;
import com.example.service.OrderService;
import com.example.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/management/financial")
public class FinancialReportController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8085";

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getFinancialDashboard(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        // Lấy data từ order service
        List<Order> allOrders = orderService.getAllOrders();
        
        // Lấy payment data từ payment service
        Map<String, Object> paymentData = getPaymentData(token);
        
        // Tính toán doanh thu
        BigDecimal totalOrderValue = allOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal deliveredRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal cancelledLoss = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tỷ lệ thành công
        long totalOrders = allOrders.size();
        long deliveredOrders = allOrders.stream()
            .mapToLong(o -> o.getStatus() == Order.OrderStatus.DELIVERED ? 1 : 0)
            .sum();
        double deliveryRate = totalOrders > 0 ? (double) deliveredOrders / totalOrders * 100 : 0;
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalOrderValue", totalOrderValue);
        dashboard.put("deliveredRevenue", deliveredRevenue);
        dashboard.put("cancelledLoss", cancelledLoss);
        dashboard.put("netRevenue", deliveredRevenue.subtract(cancelledLoss));
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("deliveredOrders", deliveredOrders);
        dashboard.put("deliverySuccessRate", deliveryRate);
        
        // Thêm payment data
        if (paymentData != null) {
            dashboard.put("paymentIncome", paymentData.get("totalIncome"));
            dashboard.put("pendingPayments", paymentData.get("pendingAmount"));
            dashboard.put("paymentSuccessRate", paymentData.get("successRate"));
        }
        
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/money-flow-report")
    public ResponseEntity<Map<String, Object>> getMoneyFlowReport(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String period) {
        
        validateToken(token);
        
        List<Order> allOrders = orderService.getAllOrders();
        
        // Xác định thời gian
        LocalDateTime startDate = LocalDateTime.now().minusDays(
            "monthly".equals(period) ? 30 : 7
        );
        
        // Dòng tiền vào từ orders đã confirm/deliver
        Map<String, BigDecimal> dailyIncome = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED || 
                        o.getStatus() == Order.OrderStatus.DELIVERED)
            .filter(o -> o.getCreatedAt().isAfter(startDate))
            .collect(Collectors.groupingBy(
                o -> o.getCreatedAt().toLocalDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
            ));
        
        // Dòng tiền mất từ orders bị cancel
        Map<String, BigDecimal> dailyLoss = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .filter(o -> o.getCreatedAt().isAfter(startDate))
            .collect(Collectors.groupingBy(
                o -> o.getCreatedAt().toLocalDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
            ));
        
        // Dòng tiền theo phương thức thanh toán
        Map<String, BigDecimal> incomeByPaymentMethod = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .filter(o -> o.getPaymentMethod() != null)
            .collect(Collectors.groupingBy(
                Order::getPaymentMethod,
                Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
            ));
        
        Map<String, Object> report = new HashMap<>();
        report.put("period", period != null ? period : "weekly");
        report.put("dailyIncome", dailyIncome);
        report.put("dailyLoss", dailyLoss);
        report.put("incomeByPaymentMethod", incomeByPaymentMethod);
        
        // Tổng kết
        BigDecimal totalIncome = dailyIncome.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLoss = dailyLoss.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("totalIncome", totalIncome);
        report.put("totalLoss", totalLoss);
        report.put("netFlow", totalIncome.subtract(totalLoss));
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/order-payment-reconciliation")
    public ResponseEntity<Map<String, Object>> getOrderPaymentReconciliation(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        List<Order> allOrders = orderService.getAllOrders();
        Map<String, Object> paymentData = getPaymentData(token);
        
        // Orders có payment thành công
        long ordersWithSuccessfulPayment = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED || 
                        o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        
        // Orders bị cancel
        long cancelledOrders = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
        
        // Orders pending
        long pendingOrders = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
            .count();
        
        Map<String, Object> reconciliation = new HashMap<>();
        reconciliation.put("totalOrders", allOrders.size());
        reconciliation.put("ordersWithSuccessfulPayment", ordersWithSuccessfulPayment);
        reconciliation.put("cancelledOrders", cancelledOrders);
        reconciliation.put("pendingOrders", pendingOrders);
        
        if (paymentData != null) {
            reconciliation.put("totalPaymentTransactions", paymentData.get("totalTransactions"));
            reconciliation.put("completedPayments", paymentData.get("completedCount"));
            reconciliation.put("failedPayments", paymentData.get("failedCount"));
        }
        
        return ResponseEntity.ok(reconciliation);
    }
    
    private Map<String, Object> getPaymentData(String token) {
        try {
            String url = PAYMENT_SERVICE_URL + "/api/management/payments/statistics";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", token);
            
            org.springframework.http.HttpEntity<String> entity = 
                new org.springframework.http.HttpEntity<>(headers);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                Map.class
            ).getBody();
            
            return response;
        } catch (Exception e) {
            // Log error nhưng không fail request
            return null;
        }
    }
    
    private void validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header required");
        }
        
        String jwt = token.substring(7);
        if (!jwtUtil.validateToken(jwt)) {
            throw new RuntimeException("Invalid or expired token");
        }
    }
}