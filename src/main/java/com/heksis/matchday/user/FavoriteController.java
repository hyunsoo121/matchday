package com.heksis.matchday.user;

import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.user.dto.FavoriteRequest;
import com.heksis.matchday.user.dto.FavoriteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  @GetMapping
  public ApiResponse<List<FavoriteResponse>> getAll(@AuthenticationPrincipal Long userId) {
    return ApiResponse.ok(favoriteService.findAll(userId));
  }

  @PostMapping
  public ApiResponse<FavoriteResponse> add(
      @AuthenticationPrincipal Long userId, @RequestBody FavoriteRequest request) {
    return ApiResponse.ok(favoriteService.add(userId, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(
      @AuthenticationPrincipal Long userId, @PathVariable Long id) {
    favoriteService.remove(userId, id);
    return ApiResponse.ok(null);
  }
}
