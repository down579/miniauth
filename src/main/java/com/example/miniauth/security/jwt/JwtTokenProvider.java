package com.example.miniauth.security.jwt;

import com.example.miniauth.config.JwtProperties;
import com.example.miniauth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(
                properties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMs = properties.expirationMs();
    }

    public String createToken(CustomUserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("pid", user.getPublicId())
                .claim("uid", user.getId())
                .claim("name", user.getDisplayName())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public CustomUserDetails parseUser(String token) {
        Claims claims = parseClaims(token);
        Long uid = claims.get("uid", Number.class).longValue();
        String publicId = claims.get("pid", String.class);
        String email = claims.getSubject();
        String displayName = claims.get("name", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return CustomUserDetails.fromTokenClaims(
                uid,
                publicId,
                email,
                displayName,
                roles == null ? List.of() : roles
        );
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
