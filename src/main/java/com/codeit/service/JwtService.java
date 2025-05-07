package com.codeit.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service for generating and validating JWT tokens.
 */
@Service
public class JwtService {

    private final String JWT_SECRET = "your-jwt-secret-key";
    private final long JWT_EXPIRATION = 24 * 60 * 60 * 1000L; // 1 day in milliseconds

    /**
     * Validates the given JWT token.
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(JWT_SECRET.getBytes())
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from the JWT token.
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET.getBytes())
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Generates a JWT token for the given username.
     * @param username the username to include in the token
     * @return the generated JWT token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET.getBytes())
                .compact();
    }
}
