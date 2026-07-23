package com.ultrahpm.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:ultrahpm-super-secret-key-for-jwt-signing-minimum-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24hrs
    private Long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<String> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                if (claims.getExpiration().before(new Date())) {
                    return null;
                }

                return claims.getSubject();
            } catch (Exception e) {
                return null;
            }
        });
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
