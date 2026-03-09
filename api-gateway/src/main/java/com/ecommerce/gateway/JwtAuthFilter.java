package com.ecommerce.gateway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    // GlobalFilter — runs on EVERY request through the gateway
    // Ordered — lets us set priority (lower number = runs first)

    @Value("${jwt.secret}")
    private String secret;

    // Public paths — these bypass JWT validation
    // /auth/** = login, register (how would you get a token otherwise?)
    // /actuator/** = health checks (needed by Docker, monitoring tools)
    private static final List<String> PUBLIC_PATHS = List.of(
        "/auth/login",
        "/auth/register",
        "/auth/health",
        "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Check if this path is public — skip JWT check if so
        boolean isPublic = PUBLIC_PATHS.stream()
            .anyMatch(path::startsWith);

        if (isPublic) {
            // Just forward the request — no auth needed
            return chain.filter(exchange);
        }

        // ── From here: path requires authentication ──

        // Step 1: Get Authorization header
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        // No header at all → 401 Unauthorized
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rejectRequest(exchange, HttpStatus.UNAUTHORIZED,
                "Missing or invalid Authorization header");
        }

        // Step 2: Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        // Step 3: Validate token
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            // Step 4: Token is valid!
            // Extract user info and add as headers for downstream services
            // Services can read these headers to know WHO is making the request
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // Mutate the request — add user info as headers
            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .header("X-User-Name", username)
                    // Services read this to know who's logged in
                    .header("X-User-Role", role)
                    // Services read this for authorization decisions
                    .build())
                .build();

            // Forward the MUTATED request (with extra headers) to the service
            return chain.filter(mutatedExchange);

        } catch (ExpiredJwtException e) {
            // Token existed but expired — tell client to login again
            return rejectRequest(exchange, HttpStatus.UNAUTHORIZED,
                "Token has expired");
        } catch (JwtException e) {
            // Token is invalid/tampered — could be an attack
            return rejectRequest(exchange, HttpStatus.FORBIDDEN,
                "Invalid token");
        }
    }

    // Helper — reject request with appropriate HTTP status
    private Mono<Void> rejectRequest(ServerWebExchange exchange,
                                      HttpStatus status,
                                      String message) {
        exchange.getResponse().setStatusCode(status);
        // getResponse().setComplete() closes the response without a body
        // For a real API you'd write a JSON error body here
        return exchange.getResponse().setComplete();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public int getOrder() {
        // -1 means run BEFORE all other filters
        // This ensures auth check happens first, before routing
        return -1;
    }
}