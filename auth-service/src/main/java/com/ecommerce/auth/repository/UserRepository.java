package com.ecommerce.auth.repository;

import com.ecommerce.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA auto-generates SQL from method name!
    // This becomes: SELECT * FROM users WHERE username = ?
    // Optional<> means "might return null" — forces you to handle it
    Optional<User> findByUsername(String username);

    // Spring Data JPA also auto-generates this:
    // SELECT COUNT(*) > 0 FROM users WHERE username = ?
    // Used during registration to check if username already taken
    boolean existsByUsername(String username);
}