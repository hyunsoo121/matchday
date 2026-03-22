package com.heksis.matchday.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_favorite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFavorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, columnDefinition = "favorite_target_type")
  private FavoriteTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static UserFavorite create(User user, FavoriteTargetType targetType, Long targetId) {
    UserFavorite favorite = new UserFavorite();
    favorite.user = user;
    favorite.targetType = targetType;
    favorite.targetId = targetId;
    favorite.createdAt = LocalDateTime.now();
    return favorite;
  }
}
