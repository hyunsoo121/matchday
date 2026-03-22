package com.heksis.matchday.match;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

  boolean existsByMatchIdAndTeamId(Long matchId, Long teamId);

  java.util.Optional<MatchTeam> findByMatchIdAndTeamId(Long matchId, Long teamId);
}
