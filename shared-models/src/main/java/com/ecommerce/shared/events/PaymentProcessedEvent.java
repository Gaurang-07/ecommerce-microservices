package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String paymentId;
    private String orderId;
    private double amount;
    private String status; // SUCCESS, FAILED
    private LocalDateTime processedAt;

    public PaymentProcessedEvent(String orderId, double amount, String status) {
        this.paymentId = java.util.UUID.randomUUID().toString();
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }
}
