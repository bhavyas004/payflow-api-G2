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

    public boolean validateTokenForAction(String token, String requiredRole) {
        if (isTokenExpired(token)) return false;
        String userRole = extractRole(token);
        return userRole.equalsIgnoreCase(requiredRole);
    }

    public boolean validateTokenForMultipleRoles(String token, List<String> allowedRoles) {
        if (isTokenExpired(token)) return false;
        String userRole = extractRole(token);
        System.out.println(userRole);
        return allowedRoles.contains(userRole.toUpperCase());
    }

    public boolean validateUsername(String token, String username) {
        return extractUsername(token).equalsIgnoreCase(username) && !isTokenExpired(token);
    }


    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}