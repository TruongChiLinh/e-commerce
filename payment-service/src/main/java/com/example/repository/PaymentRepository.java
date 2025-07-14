package com.example.repository;

import com.example.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
    Optional<Payment> findByPaymentReference(String paymentReference);
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.method = :method AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByMethod(@Param("method") Payment.PaymentMethod method);
}