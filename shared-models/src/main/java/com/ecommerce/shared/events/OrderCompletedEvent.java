package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private String status; // COMPLETED, CANCELLED
    private LocalDateTime completedAt;

    public OrderCompletedEvent(String orderId, String customerId, double totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.completedAt = LocalDateTime.now();
    }
}
