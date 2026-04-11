package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /*
     * KeyResolver — determines HOW to identify each "user" for rate limiting
     * Think of it as: "what is the key in Redis for this requester?"
     *
     * We define THREE resolvers for different scenarios:
     */

    // ─────────────────────────────────────────
    // Resolver 1: By IP Address
    // Used for public endpoints (login, register)
    // Key in Redis: "127.0.0.1" or "192.168.1.5"
    // ─────────────────────────────────────────
    @Bean
@Primary
public KeyResolver ipKeyResolver() {
    return exchange -> {
        try {
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            return Mono.just(ip);
        } catch (Exception e) {
            return Mono.just("anonymous");
        }
    };
}

    // ─────────────────────────────────────────
    // Resolver 2: By Authenticated User
    // Reads X-User-Name header that our JwtAuthFilter adds
    // Key in Redis: "gaurang" or "admin"
    // ─────────────────────────────────────────
    @Bean
public KeyResolver userKeyResolver() {
    return exchange -> {
        // Try X-User-Name header first (added by JwtAuthFilter)
        String username = exchange.getRequest()
            .getHeaders()
            .getFirst("X-User-Name");

        // Always return something — never empty!
        // Empty key causes 405 bug in Spring Cloud Gateway
        if (username != null && !username.isEmpty()) {
            return Mono.just(username);
        }

        // Fall back to IP address
        try {
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            return Mono.just(ip);
        } catch (Exception e) {
            // Last resort — return a default key
            return Mono.just("anonymous");
        }
    };
}

    // ─────────────────────────────────────────
    // Resolver 3: By IP + Path combination
    // More granular — same IP gets separate limits per endpoint
    // Key in Redis: "127.0.0.1:/api/orders"
    // ─────────────────────────────────────────
    @Bean
    public KeyResolver ipAndPathKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
            String path = exchange.getRequest()
                .getURI()
                .getPath();
            return Mono.just(ip + ":" + path);
        };
    }

    // ─────────────────────────────────────────
    // RedisRateLimiter Bean — the actual limiter
    // This uses Token Bucket algorithm internally
    @Bean
    @Primary  // default resolver when none specified
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(
            10,   // replenishRate: 10 tokens/second
            20,   // burstCapacity: max 20 tokens in bucket
            1     // requestedTokens: each request costs 1 token
        );
    }

    @Bean
    public RedisRateLimiter authRateLimiter() {
        // Stricter for auth endpoints — prevent brute force
        return new RedisRateLimiter(
            5,    // only 5 requests/second
            10,   // burst up to 10
            1
        );
    }

    @Bean
    public RedisRateLimiter paymentRateLimiter() {
        // Very strict for payments — financial operations
        return new RedisRateLimiter(
            2,    // only 2 requests/second
            5,    // burst up to 5
            1
        );
    }
}