package com.heksis.matchday.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

  List<UserFavorite> findAllByUserId(Long userId);

  Optional<UserFavorite> findByUserIdAndTargetTypeAndTargetId(
      Long userId, FavoriteTargetType targetType, Long targetId);

  boolean existsByUserIdAndTargetTypeAndTargetId(
      Long userId, FavoriteTargetType targetType, Long targetId);
}
