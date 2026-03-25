package com.heksis.matchday.user.dto;

import com.heksis.matchday.user.FavoriteTargetType;
import com.heksis.matchday.user.UserFavorite;
import java.time.LocalDateTime;

public record FavoriteResponse(
    Long id, FavoriteTargetType targetType, Long targetId, LocalDateTime createdAt) {

  public static FavoriteResponse from(UserFavorite favorite) {
    return new FavoriteResponse(
        favorite.getId(),
        favorite.getTargetType(),
        favorite.getTargetId(),
        favorite.getCreatedAt());
  }
}
