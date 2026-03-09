package com.ecommerce.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for REST APIs
            // CSRF protects browser form submissions (stateful)
            // JWT APIs are stateless — CSRF doesn't apply
            .csrf(csrf -> csrf.disable())

            // Configure which endpoints need auth
            .authorizeHttpRequests(auth -> auth
                // These endpoints are PUBLIC — no token needed
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Everything else requires authentication
                // (auth-service only has /auth/** so this is just for safety)
                .anyRequest().authenticated()
            )

            // STATELESS — don't create HTTP sessions
            // Each request must carry its own JWT — server remembers nothing
            // This is the key difference from traditional session-based auth!
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder with default cost factor 10
        // Cost factor 10 means 2^10 = 1024 rounds of hashing
        // Takes ~100ms to hash — slow enough to deter brute force
        // but fast enough for normal login
        return new BCryptPasswordEncoder();
    }
}