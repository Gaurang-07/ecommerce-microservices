package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private LocalDateTime createdAt;

    public OrderCreatedEvent(String customerId, double totalAmount) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
    }
}
