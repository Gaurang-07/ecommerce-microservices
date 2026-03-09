package com.ecommerce.auth.controller;

import com.ecommerce.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ─────────────────────────────────────────
    // POST /auth/register
    // Public endpoint — no token needed
    // Body: { "username": "gaurang", "password": "1234", "role": "USER" }
    // ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody Map<String, String> request) {

        // @RequestBody maps JSON body → Map automatically
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");

        // Basic validation
        if (username == null || password == null ||
            username.isEmpty() || password.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Username and password are required"));
        }

        Map<String, String> response = authService.register(username, password, role);

        // If service returned an error key, send 400
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────
    // POST /auth/login
    // Public endpoint — no token needed (how else would you get one?)
    // Body: { "username": "gaurang", "password": "1234" }
    // Returns: { "token": "eyJ...", "type": "Bearer" }
    // ─────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null ||
            username.isEmpty() || password.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Username and password are required"));
        }

        Map<String, String> response = authService.login(username, password);

        if (response.containsKey("error")) {
            // 401 Unauthorized — credentials are wrong
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────
    // POST /auth/validate
    // Called by API Gateway to verify tokens
    // Header: Authorization: Bearer <token>
    // ─────────────────────────────────────────
    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validate(
            @RequestHeader("Authorization") String authHeader) {

        // Auth header format: "Bearer eyJhbGci..."
        // We need just the token part — strip "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Invalid authorization header format"));
        }

        // substring(7) removes "Bearer " (7 characters)
        String token = authHeader.substring(7);
        Map<String, String> response = authService.validate(token);

        if ("false".equals(response.get("valid"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────
    // GET /auth/health
    // Simple health check — useful for testing without Postman
    // ─────────────────────────────────────────
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Auth service is running"));
    }
}