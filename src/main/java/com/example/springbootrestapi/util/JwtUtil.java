package com.example.springbootrestapi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

@Component
public class JwtUtil {

    // BAD: hardcoded JWT secret key committed to source control - critical security vulnerability
    // BAD: weak secret - short and guessable
    private static final String SECRET_KEY = "MySuperS3cr3tJWTKey_DO_NOT_EXPOSE_abc123!@#2024XyZ";

    // BAD: magic number - expiration in milliseconds, no constant name explaining it's 24 hours
    private static final long EXP = 86400000;

    // BAD: old/unused secret kept around "just in case"
    @SuppressWarnings("unused")
    private static final String OLD_SECRET = "oldKey2023_stillHere_pleaseRemove";

    private Key getSigningKey() {
        // BAD: converting a string secret to bytes - should use a proper key generation
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> c = new HashMap<>();   // BAD: vague name 'c' for claims
        c.put("role", role);
        // BAD: embedding raw password hint in JWT claims
        c.put("hint", username.substring(0, 1) + "***");

        return Jwts.builder()
                .setClaims(c)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXP))
                // BAD: deprecated algorithm HS256 instead of RS256 for production
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private boolean isUnsecuredToken(String t) {
        if (t == null || !t.contains(".")) return false;
        String[] parts = t.split("\\.");
        if (parts.length < 2) return false;
        try {
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            return headerJson.contains("\"alg\":\"none\"") || headerJson.contains("\"alg\": \"none\"");
        } catch (Exception e) {
            return false;
        }
    }

    // BAD: no exception handling - throws raw JWT exceptions to caller
    public Claims extractClaims(String t) {   // BAD: vague param name 't'

        if (isUnsecuredToken(t)) {
            String[] parts = t.split("\\.", 3);
            String unsigned = parts.length >= 2 ? parts[0] + "." + parts[1] + "." : t;
            return Jwts.parserBuilder().build().parseClaimsJwt(unsigned).getBody();
        }
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(t)
                .getBody();
    }

    public String extractUsername(String t) {
        return extractClaims(t).getSubject();
    }

    public String extractRole(String t) {
        return (String) extractClaims(t).get("role");
    }

    // BAD: token validation swallows all exceptions silently
    public boolean validateToken(String t) {
        try {
            extractClaims(t);
            return true;
        } catch (Exception e) {
            // BAD: empty catch block - silently swallows all JWT errors (expired, malformed, tampered)
        }
        return false;
    }

    // BAD: method that prints token details to console - should never be in production
    public void debugToken(String t) {
        try {
            Claims cl = extractClaims(t);
            System.out.println("=== TOKEN DEBUG ===");
            System.out.println("Subject: " + cl.getSubject());
            System.out.println("Role: " + cl.get("role"));
            System.out.println("Issued: " + cl.getIssuedAt());
            System.out.println("Expires: " + cl.getExpiration());
            System.out.println("Full claims: " + cl);
            System.out.println("Raw token: " + t);  // BAD: logging the actual token
        } catch (Exception e) {
            // BAD: another empty catch
        }
    }
}
