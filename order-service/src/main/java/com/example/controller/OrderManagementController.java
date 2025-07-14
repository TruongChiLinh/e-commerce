package com.example.controller;

import com.example.entity.Order;
import com.example.service.OrderService;
import com.example.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/management/orders")
public class OrderManagementController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/status-overview")
    public ResponseEntity<Map<String, Object>> getOrderStatusOverview(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        List<Order> allOrders = orderService.getAllOrders();
        
        // Đếm orders theo status
        Map<String, Long> statusCount = allOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getStatus().toString(),
                Collectors.counting()
            ));
        
        // Tính tổng giá trị orders theo status
        Map<String, BigDecimal> statusValue = allOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getStatus().toString(),
                Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
            ));
        
        // Đơn hàng đã giao (DELIVERED)
        long deliveredCount = allOrders.stream()
            .mapToLong(o -> o.getStatus() == Order.OrderStatus.DELIVERED ? 1 : 0)
            .sum();
            
        BigDecimal deliveredValue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Đơn hàng đã hủy (CANCELLED)
        long cancelledCount = allOrders.stream()
            .mapToLong(o -> o.getStatus() == Order.OrderStatus.CANCELLED ? 1 : 0)
            .sum();
            
        BigDecimal cancelledValue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalOrders", allOrders.size());
        overview.put("statusCount", statusCount);
        overview.put("statusValue", statusValue);
        overview.put("deliveredOrders", deliveredCount);
        overview.put("deliveredValue", deliveredValue);
        overview.put("cancelledOrders", cancelledCount);
        overview.put("cancelledValue", cancelledValue);
        
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        validateToken(token);
        
        List<Order> orders = orderService.getAllOrders();
        
        // Filter by status
        if (status != null && !status.isEmpty()) {
            orders = orders.stream()
                .filter(o -> o.getStatus().toString().equalsIgnoreCase(status))
                .toList();
        }
        
        // Manual pagination
        int start = Math.min(page * size, orders.size());
        int end = Math.min(start + size, orders.size());
        List<Order> pageContent = orders.subList(start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", pageContent);
        response.put("totalCount", orders.size());
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/order/{id}")
    public ResponseEntity<Order> getOrderDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        validateToken(token);
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/order/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        validateToken(token);
        
        String status = request.get("status");
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        
        Order updatedOrder = orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok(updatedOrder);
    }
    
    @GetMapping("/delivered")
    public ResponseEntity<Map<String, Object>> getDeliveredOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        validateToken(token);
        
        List<Order> deliveredOrders = orderService.getOrdersByStatus(Order.OrderStatus.DELIVERED);
        
        // Manual pagination
        int start = Math.min(page * size, deliveredOrders.size());
        int end = Math.min(start + size, deliveredOrders.size());
        List<Order> pageContent = deliveredOrders.subList(start, end);
        
        BigDecimal totalDeliveredValue = deliveredOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deliveredOrders", pageContent);
        response.put("totalCount", deliveredOrders.size());
        response.put("totalValue", totalDeliveredValue);
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cancelled")
    public ResponseEntity<Map<String, Object>> getCancelledOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        validateToken(token);
        
        List<Order> cancelledOrders = orderService.getOrdersByStatus(Order.OrderStatus.CANCELLED);
        
        // Manual pagination
        int start = Math.min(page * size, cancelledOrders.size());
        int end = Math.min(start + size, cancelledOrders.size());
        List<Order> pageContent = cancelledOrders.subList(start, end);
        
        BigDecimal totalCancelledValue = cancelledOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cancelledOrders", pageContent);
        response.put("totalCount", cancelledOrders.size());
        response.put("totalValue", totalCancelledValue);
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/revenue-report")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        List<Order> allOrders = orderService.getAllOrders();
        
        // Doanh thu từ đơn hàng đã giao
        BigDecimal confirmedRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED || 
                        o.getStatus() == Order.OrderStatus.DELIVERED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tổn thất từ đơn hàng bị hủy
        BigDecimal lostRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Doanh thu theo ngày (7 ngày gần nhất)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Map<String, BigDecimal> dailyRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .filter(o -> o.getCreatedAt().isAfter(sevenDaysAgo))
            .collect(Collectors.groupingBy(
                o -> o.getCreatedAt().toLocalDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
            ));
        
        Map<String, Object> report = new HashMap<>();
        report.put("confirmedRevenue", confirmedRevenue);
        report.put("lostRevenue", lostRevenue);
        report.put("dailyRevenue", dailyRevenue);
        report.put("netRevenue", confirmedRevenue.subtract(lostRevenue));
        
        return ResponseEntity.ok(report);
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