package com.ecommerce.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")  // maps to 'users' table in PostgreSQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = PostgreSQL auto-increment (serial/bigserial)
    // Alternatives: UUID (random), SEQUENCE (DB sequence object)
    private Long id;

    @Column(unique = true, nullable = false)
    // unique = true → DB constraint, no two users with same username
    // nullable = false → DB constraint, username required
    private String username;

    @Column(nullable = false)
    // NEVER store plain text passwords!
    // This stores the BCrypt hash e.g. "$2a$10$xyz..."
    private String password;

    @Column(nullable = false)
    // Role-based access control — "USER" or "ADMIN"
    // In real systems this would be a separate Role entity with many-to-many
    private String role;

    // Constructors
    public User() {}  // JPA requires a no-arg constructor — don't remove!

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}