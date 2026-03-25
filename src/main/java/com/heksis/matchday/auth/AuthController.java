package com.heksis.matchday.auth;

import com.heksis.matchday.auth.dto.RefreshRequest;
import com.heksis.matchday.auth.dto.TokenResponse;
import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtProvider jwtProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @PostMapping("/refresh")
  public ApiResponse<TokenResponse> refresh(@RequestBody RefreshRequest request) {
    String refreshToken = request.refreshToken();
    if (!jwtProvider.validate(refreshToken)) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
    Long userId = jwtProvider.getUserId(refreshToken);
    String stored = redisTemplate.opsForValue().get("refresh:" + userId);
    if (!refreshToken.equals(stored)) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
    return ApiResponse.ok(new TokenResponse(jwtProvider.generateAccessToken(userId), refreshToken));
  }
}
