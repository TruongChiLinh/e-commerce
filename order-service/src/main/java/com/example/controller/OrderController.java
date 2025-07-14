package com.example.controller;

import com.example.common.response.ApiResponse;
import com.example.entity.Order;
import com.example.service.OrderService;
import com.example.dto.PlaceOrderRequest;
import com.example.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse orderResponse = orderService.placeOrder(request);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", orderResponse));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, Order.OrderStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success("Order status updated", updatedOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully", null));
    }
}