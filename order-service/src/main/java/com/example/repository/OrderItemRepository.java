package com.example.repository;

import com.example.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.userId = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantityByProductId(@Param("productId") Long productId);
}