package com.ecommerce.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who this notification is for
    @Column(nullable = false)
    private String recipient;

    // ORDER_CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED
    @Column(nullable = false)
    private String type;

    // The actual message content
    @Column(nullable = false, length = 1000)
    private String message;

    // Reference to the order this notification is about
    @Column
    private String orderId;

    // EMAIL, SMS, PUSH — extensible for future channels
    @Column(nullable = false)
    private String channel;

    // SENT, FAILED, PENDING
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime sentAt;

    // JPA requires no-arg constructor
    public Notification() {}

    // Constructor for easy creation
    public Notification(String recipient, String type,
                        String message, String orderId,
                        String channel) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.orderId = orderId;
        this.channel = channel;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}