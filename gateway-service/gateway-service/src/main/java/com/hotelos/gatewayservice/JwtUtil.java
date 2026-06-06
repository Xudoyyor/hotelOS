package com.hotelos.gatewayservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // MUHIM: auth-service'dagi properties faylda turgan kalit bilan bir xil bo'lishi shart!
    private final String SECRET = "U29tZUh0dHBSZXF1ZXN0U2VjcmV0S2V5Rm9ySGVsbG9Xb3JsZHNzZGFzZGE=";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public String extractRole(String token) {
        // "role" o'rniga "roles" deb yozildi, chunki tokenda ko'plikda kelyapti
        return (String) getClaims(token).get("roles");
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}