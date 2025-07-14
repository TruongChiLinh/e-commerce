package com.example.controller;

import com.example.common.response.ApiResponse;
import com.example.entity.Payment;
import com.example.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(Payment.PaymentStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(createdPayment);
    }

    @PostMapping("/create-for-order")
    public ResponseEntity<ApiResponse<Payment>> createPaymentForOrder(@Valid @RequestBody com.example.dto.CreatePaymentRequest request) {
        Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(request.getMethod());
        Payment createdPayment = paymentService.createPaymentForOrder(
            request.getOrderId(), 
            request.getUserId(), 
            method, 
            request.getAmount()
        );
        return ResponseEntity.ok(ApiResponse.success("Payment created successfully", createdPayment));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable Long id) {
        Payment processedPayment = paymentService.processPayment(id);
        return ResponseEntity.ok(processedPayment);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(id, Payment.PaymentStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok().build();
    }
}