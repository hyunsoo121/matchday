package com.heksis.matchday.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-expiry:3600000}")
  private long accessTokenExpiry; // 1시간

  @Value("${jwt.refresh-token-expiry:2592000000}")
  private long refreshTokenExpiry; // 30일

  private SecretKey key;

  @PostConstruct
  public void init() {
    key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "access")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
        .signWith(key)
        .compact();
  }

  public String generateRefreshToken(Long userId) {
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("type", "refresh")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
        .signWith(key)
        .compact();
  }

  public Long getUserId(String token) {
    return Long.parseLong(parseClaims(token).getSubject());
  }

  public boolean validate(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
