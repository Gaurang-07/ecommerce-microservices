package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ─────────────────────────────────────────
    // Send Order Confirmation
    // Triggered when OrderCreatedEvent received
    // ─────────────────────────────────────────
    public Notification sendOrderConfirmation(String customerId,
                                               String orderId,
                                               Double amount) {
        String message = String.format(
            "🛒 Order Confirmed! Your order #%s has been placed successfully. " +
            "Total amount: ₹%.2f. We'll notify you once payment is processed.",
            orderId, amount
        );

        return sendNotification(customerId, "ORDER_CREATED",
                               message, orderId, "EMAIL");
    }

    // ─────────────────────────────────────────
    // Send Payment Success
    // Triggered when PaymentProcessedEvent with SUCCESS status
    // ─────────────────────────────────────────
    public Notification sendPaymentSuccess(String customerId,
                                           String orderId,
                                           Double amount) {
        String message = String.format(
            "✅ Payment Successful! ₹%.2f has been charged for order #%s. " +
            "Your order is now being processed. Thank you for shopping with us!",
            amount, orderId
        );

        return sendNotification(customerId, "PAYMENT_SUCCESS",
                               message, orderId, "EMAIL");
    }

    // ─────────────────────────────────────────
    // Send Payment Failed
    // Triggered when PaymentProcessedEvent with FAILED status
    // ─────────────────────────────────────────
    public Notification sendPaymentFailed(String customerId,
                                          String orderId,
                                          Double amount) {
        String message = String.format(
            "❌ Payment Failed! We couldn't process ₹%.2f for order #%s. " +
            "Please check your payment details and try again.",
            amount, orderId
        );

        return sendNotification(customerId, "PAYMENT_FAILED",
                               message, orderId, "EMAIL");
    }

    // ─────────────────────────────────────────
    // Core send method — simulates actual sending
    // In production: integrate SendGrid/AWS SES/Twilio here
    // ─────────────────────────────────────────
    private Notification sendNotification(String recipient, String type,
                                          String message, String orderId,
                                          String channel) {
        // Create notification record
        Notification notification = new Notification(
            recipient, type, message, orderId, channel
        );

        // Simulate sending
        // In real system: emailService.send(recipient, message)
        // or smsService.send(phoneNumber, message)
        boolean sendSuccess = simulateSend(recipient, message, channel);

        if (sendSuccess) {
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            System.out.println("📧 NOTIFICATION SENT to " + recipient +
                             " | Type: " + type +
                             " | Message: " + message);
        } else {
            notification.setStatus("FAILED");
            System.out.println("❌ NOTIFICATION FAILED for " + recipient);
        }

        // Save to DB regardless — audit trail
        return notificationRepository.save(notification);
    }

    // Simulates email/SMS sending
    // Returns true 95% of time (realistic success rate)
    private boolean simulateSend(String recipient,
                                  String message,
                                  String channel) {
        // Simulate occasional failures (5% failure rate)
        return Math.random() > 0.05;
    }

    // ─────────────────────────────────────────
    // Query methods for REST API
    // ─────────────────────────────────────────
    public List<Notification> getNotificationsForRecipient(String recipient) {
        return notificationRepository
            .findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public List<Notification> getNotificationsForOrder(String orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatus("FAILED");
    }
}