package com.example.service;

import com.example.common.exception.ResourceNotFoundException;
import com.example.entity.Payment;
import com.example.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final double SUCCESS_RATE = 0.9;

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public Payment createPayment(Payment payment) {
        // Generate unique payment reference
        payment.setPaymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment processPayment(Long id) {
        Payment payment = getPaymentById(id);
        
        // Simulate payment processing
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setProcessedAt(LocalDateTime.now());
        
        // Simulate payment gateway response
        if (Math.random() > (1 - SUCCESS_RATE)) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setGatewayResponse("Payment processed successfully");
            logger.info("Payment completed successfully: {}", payment.getPaymentReference());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setGatewayResponse("Payment failed - insufficient funds");
            logger.warn("Payment failed: {}", payment.getPaymentReference());
        }
        
        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Long id, Payment.PaymentStatus status) {
        Payment payment = getPaymentById(id);
        payment.setStatus(status);
        
        if (status == Payment.PaymentStatus.COMPLETED || status == Payment.PaymentStatus.FAILED) {
            payment.setProcessedAt(LocalDateTime.now());
        }
        
        return paymentRepository.save(payment);
    }

    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }

    public List<Payment> getCompletedPaymentsByMethod(Payment.PaymentMethod method) {
        return paymentRepository.findCompletedPaymentsByMethod(method);
    }

    public Payment createPaymentForOrder(Long orderId, Long userId, Payment.PaymentMethod method, java.math.BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setMethod(method);
        payment.setAmount(amount);
        return createPayment(payment);
    }
}