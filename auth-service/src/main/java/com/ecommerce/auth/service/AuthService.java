package com.ecommerce.auth.service;

import com.ecommerce.auth.model.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // PasswordEncoder is a bean we'll define in SecurityConfig
    // Spring injects it here automatically

    @Autowired
    private JwtUtil jwtUtil;

    // ─────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────
    public Map<String, String> register(String username, String password, String role) {
        Map<String, String> response = new HashMap<>();

        // Check if username already taken
        if (userRepository.existsByUsername(username)) {
            response.put("error", "Username already exists");
            return response;
        }

        // NEVER save plain text password!
        // BCrypt hash looks like: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
        // The $10$ part is the "cost factor" — how many rounds of hashing
        // Higher cost = slower to hash = harder to brute force
        String hashedPassword = passwordEncoder.encode(password);

        // Default role to USER if not specified
        String userRole = (role != null && !role.isEmpty()) ? role : "USER";

        User user = new User(username, hashedPassword, userRole);
        userRepository.save(user);

        response.put("message", "User registered successfully");
        response.put("username", username);
        response.put("role", userRole);
        return response;
    }

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────
    public Map<String, String> login(String username, String password) {
        Map<String, String> response = new HashMap<>();

        // Step 1: Find user by username
        User user = userRepository.findByUsername(username)
            .orElse(null);  // returns null if not found

        if (user == null) {
            response.put("error", "Invalid username or password");
            // Notice: we don't say "username not found" specifically
            // That would tell attackers which usernames exist (enumeration attack)
            return response;
        }

        // Step 2: Verify password
        // passwordEncoder.matches() hashes the input and compares
        // We NEVER decrypt the stored hash — BCrypt is one-way!
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("error", "Invalid username or password");
            return response;
        }

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("type", "Bearer");
        // "Bearer" is the token type — standard for JWT
        // Client sends it as: Authorization: Bearer <token>
        return response;
    }

    // ─────────────────────────────────────────
    // VALIDATE (called by gateway to verify token)
    // ─────────────────────────────────────────
    public Map<String, String> validate(String token) {
        Map<String, String> response = new HashMap<>();

        if (jwtUtil.validateToken(token)) {
            response.put("valid", "true");
            response.put("username", jwtUtil.extractUsername(token));
            response.put("role", jwtUtil.extractRole(token));
        } else {
            response.put("valid", "false");
        }
        return response;
    }
}