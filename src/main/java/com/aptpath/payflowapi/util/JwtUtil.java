package com.aptpath.payflowapi.util;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final String SECRET = "81DAB887CBC2C21AEF44211B28B156CF305181B5E7DAAD588B948C12392534C3";
    private final long EXPIRATION = 3600000; // 1 hour

    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
            .setSubject(username)
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }
    
    // Extract employee ID from token (if present)
    public Long extractEmployeeId(String token) {
        return extractAllClaims(token).get("employeeId", Long.class);
    }
    
    // Extract employee name from token (if present) - use fullName instead
    public String extractFullName(String token) {
        return extractAllClaims(token).get("fullName", String.class);
    }
    
    // Extract manager from token (if present)
    public String extractManager(String token) {
        return extractAllClaims(token).get("manager", String.class);
    }
    
    // Extract age from token (if present)
    public Integer extractAge(String token) {
        return extractAllClaims(token).get("age", Integer.class);
    }
    
    // Extract contact number from token (if present)
    public String extractContactNumber(String token) {
        return extractAllClaims(token).get("contactNumber", String.class);
    }
    
    // Extract created at from token (if present)
    public String extractCreatedAt(String token) {
        return extractAllClaims(token).get("createdAt", String.class);
    }
    
    // Extract employee status from token (if present)
    public String extractStatus(String token) {
        return extractAllClaims(token).get("status", String.class);
    }

    public boolean validateTokenForAction(String token, String requiredRole) {
        if (isTokenExpired(token)) return false;
        String userRole = extractRole(token);
        return userRole.equalsIgnoreCase(requiredRole);
    }

    public boolean validateTokenForMultipleRoles(String token, List<String> allowedRoles) {
        if (isTokenExpired(token)) return false;
        String userRole = extractRole(token);
        return allowedRoles.contains(userRole.toUpperCase());
    }

    public boolean validateUsername(String token, String username) {
        return extractUsername(token).equalsIgnoreCase(username) && !isTokenExpired(token);
    }


    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}