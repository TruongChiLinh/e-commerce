package com.example.controller;

import com.example.entity.Payment;
import com.example.service.PaymentService;
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
@RequestMapping("/api/management/payments")
public class PaymentManagementController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/money-flow")
    public ResponseEntity<Map<String, Object>> getMoneyFlow(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        List<Payment> allPayments = paymentService.getAllPayments();
        
        // Tổng dòng tiền
        BigDecimal totalIncome = allPayments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal pendingAmount = allPayments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal failedAmount = allPayments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.FAILED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Dòng tiền theo phương thức thanh toán
        Map<String, BigDecimal> incomeByMethod = allPayments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .collect(Collectors.groupingBy(
                p -> p.getMethod().toString(),
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
            ));
        
        // Dòng tiền theo ngày (7 ngày gần nhất)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Map<String, BigDecimal> dailyIncome = allPayments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
            .filter(p -> p.getProcessedAt() != null && p.getProcessedAt().isAfter(sevenDaysAgo))
            .collect(Collectors.groupingBy(
                p -> p.getProcessedAt().toLocalDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
            ));
        
        Map<String, Object> moneyFlow = new HashMap<>();
        moneyFlow.put("totalIncome", totalIncome);
        moneyFlow.put("pendingAmount", pendingAmount);
        moneyFlow.put("failedAmount", failedAmount);
        moneyFlow.put("incomeByPaymentMethod", incomeByMethod);
        moneyFlow.put("dailyIncome", dailyIncome);
        moneyFlow.put("totalTransactions", allPayments.size());
        
        return ResponseEntity.ok(moneyFlow);
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        validateToken(token);
        
        List<Payment> payments = paymentService.getAllPayments();
        
        // Filter by status
        if (status != null && !status.isEmpty()) {
            payments = payments.stream()
                .filter(p -> p.getStatus().toString().equalsIgnoreCase(status))
                .toList();
        }
        
        // Manual pagination
        int start = Math.min(page * size, payments.size());
        int end = Math.min(start + size, payments.size());
        List<Payment> pageContent = payments.subList(start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", pageContent);
        response.put("totalCount", payments.size());
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/transaction/{id}")
    public ResponseEntity<Payment> getTransactionDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        validateToken(token);
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestHeader("Authorization") String token) {
        
        validateToken(token);
        
        List<Payment> allPayments = paymentService.getAllPayments();
        
        long completedCount = allPayments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED ? 1 : 0)
            .sum();
            
        long failedCount = allPayments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.FAILED ? 1 : 0)
            .sum();
            
        long pendingCount = allPayments.stream()
            .mapToLong(p -> p.getStatus() == Payment.PaymentStatus.PENDING ? 1 : 0)
            .sum();
        
        double successRate = allPayments.size() > 0 ? 
            (double) completedCount / allPayments.size() * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTransactions", allPayments.size());
        stats.put("completedCount", completedCount);
        stats.put("failedCount", failedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("successRate", successRate);
        
        return ResponseEntity.ok(stats);
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