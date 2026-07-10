package com.jtech.api_gateway_spring.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Data
public class JwtTokenProvider {

    @Value("${app.jwt.secret:EnterpriseJobScraperSecretKeyMustBeVeryLongAndSecure2026!}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 Hours
    private long jwtExpirationInMs;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Cryptographic Minting: Builds the immutable payload block (Section 2)
    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 1. To extract the username (Subject):
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()                 // 🌟 Changed from parserBuilder()
                .verifyWith((javax.crypto.SecretKey) key) // 🌟 Changed from setSigningKey()
                .build()
                .parseSignedClaims(token)             // 🌟 Changed from parseClaimsJws()
                .getPayload();                        // 🌟 Changed from getBody()

        return claims.getSubject();
    }

    // 2. To validate the lifecycle:
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Captures expired, malformed, or tampered tokens safely
            return false;
        }
    }


}