package com.heksis.matchday.match.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.heksis.matchday.match.Match;
import com.heksis.matchday.match.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;

public record MatchResponse(
    Long id,
    Long sportId,
    Long leagueId,
    LocalDateTime matchTime,
    MatchStatus status,
    @JsonRawValue String scoreDetail,
    String externalUrl,
    List<MatchTeamResponse> teams) {

  public static MatchResponse from(Match match) {
    return new MatchResponse(
        match.getId(),
        match.getSport().getId(),
        match.getLeague() != null ? match.getLeague().getId() : null,
        match.getMatchTime(),
        match.getStatus(),
        match.getScoreDetail(),
        match.getExternalUrl(),
        match.getTeams().stream().map(MatchTeamResponse::from).toList());
  }
}
