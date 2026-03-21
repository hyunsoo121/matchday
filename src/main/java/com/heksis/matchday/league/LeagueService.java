package com.heksis.matchday.league;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.league.dto.LeagueResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueService {

  private final LeagueRepository leagueRepository;

  public List<LeagueResponse> findBySport(Long sportId) {
    return leagueRepository.findAllBySportIdAndIsActiveTrueOrderByDisplayOrderAsc(sportId).stream()
        .map(LeagueResponse::from)
        .toList();
  }

  public LeagueResponse findById(Long id) {
    League league =
        leagueRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.LEAGUE_NOT_FOUND));
    return LeagueResponse.from(league);
  }
}
