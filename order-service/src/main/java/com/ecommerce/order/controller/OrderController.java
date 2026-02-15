package com.ecommerce.order.controller;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.shared.dto.CreateOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());
        Order order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        log.info("Fetching order: {}", orderId);
        Optional<Order> order = orderService.getOrder(orderId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status) {
        log.info("Updating order {} status to {}", orderId, status);
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return updatedOrder != null ? 
                ResponseEntity.ok(updatedOrder) : 
                ResponseEntity.notFound().build();
    }
}
