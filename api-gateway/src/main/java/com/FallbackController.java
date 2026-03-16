package com.ecommerce.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // ─────────────────────────────────────────
    // Order Service Fallback
    // Called when order-service circuit is OPEN
    // ─────────────────────────────────────────
    @GetMapping("/orders")
    @PostMapping("/orders")
    // Both GET and POST need fallbacks
    public Mono<ResponseEntity<Map<String, Object>>> ordersFallback() {
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", "Order service is temporarily unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                // Tell client when to retry — good API design!
                "retryAfter", "30 seconds"
            )));
    }

    // ─────────────────────────────────────────
    // Product Service Fallback
    // ─────────────────────────────────────────
    @GetMapping("/products")
    @PostMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> productsFallback() {
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", "Product service is temporarily unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "retryAfter", "30 seconds"
            )));
    }

    // ─────────────────────────────────────────
    // Payment Service Fallback
    // ─────────────────────────────────────────
    @GetMapping("/payments")
    @PostMapping("/payments")
    public Mono<ResponseEntity<Map<String, Object>>> paymentsFallback() {
        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                // Payment fallback message is more specific — financial impact
                "message", "Payment service is temporarily unavailable. Your payment has NOT been processed. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "retryAfter", "60 seconds"
            )));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "status", "Gateway is running",
            "timestamp", LocalDateTime.now().toString()
        )));
    }
}