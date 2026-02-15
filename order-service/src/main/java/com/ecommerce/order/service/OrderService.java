package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.shared.dto.CreateOrderRequest;
import com.ecommerce.shared.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setCustomerId(request.getCustomerId());
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order created: {}", savedOrder.getOrderId());

        // Publish event to Kafka
        OrderCreatedEvent event = new OrderCreatedEvent(
            savedOrder.getOrderId(),
            savedOrder.getCustomerId(),
            savedOrder.getTotalAmount(),
            savedOrder.getCreatedAt()
        );
        kafkaTemplate.send("order-created-topic", savedOrder.getOrderId(), event);
        log.info("Order created event published: {}", event.getOrderId());

        return savedOrder;
    }

    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Order updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }
}
