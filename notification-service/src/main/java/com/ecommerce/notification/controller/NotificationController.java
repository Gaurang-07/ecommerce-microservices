package com.ecommerce.notification.controller;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // GET /api/notifications
    // Get all notifications (admin use)
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(
            notificationService.getAllNotifications()
        );
    }

    // GET /api/notifications/recipient/{customerId}
    // Get all notifications for a specific customer
    @GetMapping("/recipient/{customerId}")
    public ResponseEntity<List<Notification>> getByRecipient(
            @PathVariable String customerId) {
        return ResponseEntity.ok(
            notificationService.getNotificationsForRecipient(customerId)
        );
    }

    // GET /api/notifications/order/{orderId}
    // Get all notifications for a specific order
    // Useful for debugging — see what was sent for an order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> getByOrder(
            @PathVariable String orderId) {
        return ResponseEntity.ok(
            notificationService.getNotificationsForOrder(orderId)
        );
    }

    // GET /api/notifications/failed
    // Get all failed notifications — for monitoring/retry
    @GetMapping("/failed")
    public ResponseEntity<List<Notification>> getFailedNotifications() {
        return ResponseEntity.ok(
            notificationService.getFailedNotifications()
        );
    }
}