package com.heksis.matchday.match;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.match.dto.MatchResponse;
import java.time.LocalDate;
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
    List<Match> matches;
    if (status != null && sportId != null) {
      matches = matchRepository.findBySportIdAndStatusWithTeams(sportId, status);
    } else if (status != null) {
      matches = matchRepository.findByStatusWithTeams(status);
    } else if (date != null && sportId != null) {
      matches =
          matchRepository.findBySportIdAndDateRangeWithTeams(
              sportId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    } else if (date != null) {
      matches =
          matchRepository.findByDateRangeWithTeams(
              date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    } else if (sportId != null) {
      matches = matchRepository.findBySportIdWithTeams(sportId);
    } else {
      matches = matchRepository.findAllWithTeams();
    }
    return matches.stream().map(MatchResponse::from).toList();
  }

  public List<MatchResponse> findToday(Long sportId) {
    return findMatches(sportId, null, null, LocalDate.now());
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
