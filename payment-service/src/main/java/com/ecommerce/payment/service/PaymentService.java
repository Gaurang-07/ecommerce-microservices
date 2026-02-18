package com.ecommerce.payment.service;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.shared.events.OrderCreatedEvent;
import com.ecommerce.shared.events.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: {}", event.getOrderId());
        processPayment(event.getOrderId(), event.getTotalAmount());
    }

    public Payment processPayment(String orderId, double amount) {
        try {
            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID().toString());
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setPaymentMethod("CARD");
            
            // Simulate payment processing
            if (simulatePaymentGateway(amount)) {
                payment.setStatus("SUCCESS");
                log.info("Payment processed successfully for order: {}", orderId);
            } else {
                payment.setStatus("FAILED");
                log.warn("Payment processing failed for order: {}", orderId);
            }
            
            payment.setCreatedAt(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Publish payment event
            PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderId,
                amount,
                payment.getStatus()
            );
            kafkaTemplate.send("payment-processed-topic", orderId, event);

            return savedPayment;
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", orderId, e);
            return null;
        }
    }

    public Optional<Payment> getPayment(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    private boolean simulatePaymentGateway(double amount) {
        // Simulate payment gateway response - 90% success rate
        return Math.random() > 0.1;
    }
}
