package com.mipt.todolist.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
  private final SecretKey secretKey;
  private final Duration expiration;

  public JwtUtils(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") Duration expiration) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    this.expiration = expiration;
  }

  public String generateToken(UserDetails userDetails) {
    Instant now = Instant.now();
    List<String> authorities = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    return Jwts.builder()
        .subject(userDetails.getUsername())
        .claim("authorities", authorities)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiration)))
        .signWith(secretKey)
        .compact();
  }

  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    Claims claims = extractAllClaims(token);
    return claims.getSubject().equals(userDetails.getUsername())
        && claims.getExpiration().after(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
