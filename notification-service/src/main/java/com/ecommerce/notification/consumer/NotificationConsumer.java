package com.ecommerce.notification.consumer;

import com.ecommerce.shared.events.OrderCreatedEvent;
import com.ecommerce.shared.events.PaymentProcessedEvent;
import com.ecommerce.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;

    // ─────────────────────────────────────────
    // Listen to Order Created Events
    // ─────────────────────────────────────────
    @KafkaListener(
        topics = "order-created-topic",
        // Must match group-id in application.yml
        groupId = "notification-service-group",
        // Tell Spring how to deserialize the JSON into Java object
        containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("📨 Received OrderCreatedEvent: " + event.getOrderId());

        try {
            notificationService.sendOrderConfirmation(
                event.getCustomerId(),
                event.getOrderId(),
                event.getTotalAmount()
            );
        } catch (Exception e) {
            // Log but don't rethrow — we don't want Kafka to retry
            // endlessly for notification failures
            System.err.println("Failed to send order notification: "
                + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // Listen to Payment Processed Events
    // ─────────────────────────────────────────
    @KafkaListener(
        topics = "payment-processed-topic",
        groupId = "notification-service-group",
        containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        System.out.println("📨 Received PaymentProcessedEvent: "
            + event.getOrderId()
            + " Status: " + event.getStatus());

        try {
            if ("SUCCESS".equals(event.getStatus())) {
                notificationService.sendPaymentSuccess(
                    event.getCustomerId(),
                    event.getOrderId(),
                    event.getAmount()
                );
            } else {
                notificationService.sendPaymentFailed(
                    event.getCustomerId(),
                    event.getOrderId(),
                    event.getAmount()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to send payment notification: "
                + e.getMessage());
        }
    }
}