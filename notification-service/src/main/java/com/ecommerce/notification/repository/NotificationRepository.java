package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a specific recipient
    // SQL: SELECT * FROM notifications WHERE recipient = ? ORDER BY created_at DESC
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);

    // Get all notifications for a specific order
    List<Notification> findByOrderId(String orderId);

    // Get notifications by type
    List<Notification> findByType(String type);

    // Get notifications by status — useful for retry logic
    // e.g. find all FAILED notifications and retry them
    List<Notification> findByStatus(String status);
}