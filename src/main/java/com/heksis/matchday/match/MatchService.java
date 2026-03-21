package com.heksis.matchday.match;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.match.dto.MatchResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

  private final MatchRepository matchRepository;

  public List<MatchResponse> findMatches(
      Long sportId, Long leagueId, MatchStatus status, LocalDate date) {
    LocalDateTime from = date != null ? date.atStartOfDay() : null;
    LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : null;
    return matchRepository.findWithFilters(sportId, leagueId, status, from, to).stream()
        .map(MatchResponse::from)
        .toList();
  }

  public List<MatchResponse> findToday(Long sportId) {
    return findMatches(sportId, null, null, LocalDate.now());
  }

  public List<MatchResponse> findLive(Long sportId) {
    LocalDateTime from = null;
    LocalDateTime to = null;
    return matchRepository.findWithFilters(sportId, null, MatchStatus.LIVE, from, to).stream()
        .map(MatchResponse::from)
        .toList();
  }

  public MatchResponse findById(Long id) {
    Match match =
        matchRepository
            .findByIdWithTeams(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
    return MatchResponse.from(match);
  }
}
