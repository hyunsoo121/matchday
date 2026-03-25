package com.heksis.matchday.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  @Value("${jwt.refresh-token-expiry:2592000000}")
  private long refreshTokenExpiry;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Long userId = ((Number) oAuth2User.getAttribute("userId")).longValue();

    String accessToken = jwtProvider.generateAccessToken(userId);
    String refreshToken = jwtProvider.generateRefreshToken(userId);

    redisTemplate
        .opsForValue()
        .set("refresh:" + userId, refreshToken, refreshTokenExpiry, TimeUnit.MILLISECONDS);

    response.sendRedirect(
        frontendUrl
            + "/oauth2/callback"
            + "?accessToken="
            + accessToken
            + "&refreshToken="
            + refreshToken);
  }
}
