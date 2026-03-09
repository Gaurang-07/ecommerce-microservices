package com.ecommerce.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component  // Spring manages this as a bean — injectable everywhere
public class JwtUtil {

    // Reads jwt.secret from application.yml
    // @Value injects config values directly into fields
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Convert secret string → cryptographic Key object
    // Called lazily — only when first needed
    private Key getSigningKey() {
        // Keys.hmacShaKeyFor requires at least 256 bits (32 bytes) for HS256
        // That's why our secret is long!
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ─────────────────────────────────────────
    // GENERATE TOKEN
    // Called after successful login
    // ─────────────────────────────────────────
    public String generateToken(String username, String role) {
        return Jwts.builder()
            .setSubject(username)           // 'sub' claim — who this token is for
            .claim("role", role)            // custom claim — user's role
            .setIssuedAt(new Date())        // 'iat' claim — when issued
            .setExpiration(new Date(        // 'exp' claim — when it expires
                System.currentTimeMillis() + expiration
            ))
            .signWith(getSigningKey(),      // sign with our secret key
                SignatureAlgorithm.HS256)   // using HS256 algorithm
            .compact();                     // build → returns the token string
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN
    // Called by gateway on every request
    // Returns true if valid, false if invalid/expired
    // ─────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            // parseClaimsJws does THREE things:
            // 1. Verifies signature (was it signed with our key?)
            // 2. Checks expiry (is 'exp' in the future?)
            // 3. Parses claims (extracts payload)
            // Throws exception if ANY of these fail
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token was valid but has expired
            System.out.println("JWT expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            // Token format not supported
            System.out.println("JWT unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            // Token string is not valid JWT format
            System.out.println("JWT malformed: " + e.getMessage());
        } catch (SignatureException e) {
            // Signature doesn't match — token was tampered!
            System.out.println("JWT signature invalid: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Token is null or empty
            System.out.println("JWT empty: " + e.getMessage());
        }
        return false;
    }

    // Extract username from token (without full validation)
    // Used after validateToken() confirms it's valid
    public String extractUsername(String token) {
        return getClaims(token).getSubject();  // gets 'sub' claim
    }

    // Extract role from token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);  // gets custom 'role' claim
    }

    // Parse and return all claims from token
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();  // getBody() returns the payload (Claims object)
    }
}