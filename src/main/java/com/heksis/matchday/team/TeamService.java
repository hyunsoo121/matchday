package com.heksis.matchday.team;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.team.dto.TeamResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

  private final TeamRepository teamRepository;

  public List<TeamResponse> search(String q, Long sportId) {
    return teamRepository.search(q, sportId).stream().map(TeamResponse::from).toList();
  }

  public TeamResponse findById(Long id) {
    Team team =
        teamRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    return TeamResponse.from(team);
  }
}
