package com.heksis.matchday.user;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.user.dto.FavoriteRequest;
import com.heksis.matchday.user.dto.FavoriteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final UserFavoriteRepository favoriteRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<FavoriteResponse> findAll(Long userId) {
    return favoriteRepository.findAllByUserId(userId).stream().map(FavoriteResponse::from).toList();
  }

  @Transactional
  public FavoriteResponse add(Long userId, FavoriteRequest request) {
    if (favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(
        userId, request.targetType(), request.targetId())) {
      throw new BusinessException(ErrorCode.FAVORITE_ALREADY_EXISTS);
    }
    User user = userRepository.findById(userId).orElseThrow();
    UserFavorite favorite =
        favoriteRepository.save(
            UserFavorite.create(user, request.targetType(), request.targetId()));
    return FavoriteResponse.from(favorite);
  }

  @Transactional
  public void remove(Long userId, Long favoriteId) {
    UserFavorite favorite =
        favoriteRepository
            .findById(favoriteId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_NOT_FOUND));
    if (!favorite.getUser().getId().equals(userId)) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    favoriteRepository.delete(favorite);
  }
}
