package com.heksis.matchday.match;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.match.dto.MatchResponse;
import com.heksis.matchday.user.FavoriteTargetType;
import com.heksis.matchday.user.UserFavorite;
import com.heksis.matchday.user.UserFavoriteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

  private final MatchRepository matchRepository;
  private final UserFavoriteRepository favoriteRepository;

  public List<MatchResponse> findMatches(
      Long sportId, Long leagueId, MatchStatus status, LocalDate date) {
    List<Match> matches;
    if (status != null && sportId != null) {
      matches = matchRepository.findBySportIdAndStatusWithTeams(sportId, status);
    } else if (status != null) {
      matches = matchRepository.findByStatusWithTeams(status);
    } else if (date != null && sportId != null) {
      LocalDateTime[] range = toUtcRange(date);
      matches = matchRepository.findBySportIdAndDateRangeWithTeams(sportId, range[0], range[1]);
    } else if (date != null) {
      LocalDateTime[] range = toUtcRange(date);
      matches = matchRepository.findByDateRangeWithTeams(range[0], range[1]);
    } else if (sportId != null) {
      matches = matchRepository.findBySportIdWithTeams(sportId);
    } else {
      matches = matchRepository.findAllWithTeams();
    }
    return matches.stream().map(MatchResponse::from).toList();
  }

  public List<MatchResponse> findToday(Long sportId) {
    return findMatches(sportId, null, null, LocalDate.now(ZoneId.of("Asia/Seoul")));
  }

  public List<MatchResponse> findByFavorites(Long userId, LocalDate date) {
    List<UserFavorite> favorites = favoriteRepository.findAllByUserId(userId);
    List<Long> sportIds = favorites.stream()
        .filter(f -> f.getTargetType() == FavoriteTargetType.SPORT)
        .map(UserFavorite::getTargetId).toList();
    List<Long> leagueIds = favorites.stream()
        .filter(f -> f.getTargetType() == FavoriteTargetType.LEAGUE)
        .map(UserFavorite::getTargetId).toList();
    List<Long> teamIds = favorites.stream()
        .filter(f -> f.getTargetType() == FavoriteTargetType.TEAM)
        .map(UserFavorite::getTargetId).toList();

    if (sportIds.isEmpty() && leagueIds.isEmpty() && teamIds.isEmpty()) return List.of();

    LocalDate kstDate = date != null ? date : LocalDate.now(KST);
    LocalDateTime[] range = toUtcRange(kstDate);
    return matchRepository
        .findByDateRangeAndFavoritesWithTeams(range[0], range[1], sportIds, leagueIds, teamIds)
        .stream().map(MatchResponse::from).toList();
  }

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  // KST 날짜 → UTC LocalDateTime 범위 변환
  private LocalDateTime[] toUtcRange(LocalDate kstDate) {
    ZonedDateTime startKst = kstDate.atStartOfDay(KST);
    LocalDateTime startUtc = startKst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    LocalDateTime endUtc =
        startKst.plusDays(1).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    return new LocalDateTime[] {startUtc, endUtc};
  }

  public List<MatchResponse> findLive(Long sportId) {
    return findMatches(sportId, null, MatchStatus.LIVE, null);
  }

  public MatchResponse findById(Long id) {
    Match match =
        matchRepository
            .findByIdWithTeams(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
    return MatchResponse.from(match);
  }
}
