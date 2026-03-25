package com.heksis.matchday.collector.kleague.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KleagueScheduleResponse(String resultCode, Data data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Data(List<Game> scheduleList) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Game(
      Integer gameId,
      Integer leagueId,
      Integer year,
      String gameDate,
      String gameTime,
      String homeTeam,
      String homeTeamName,
      String awayTeam,
      String awayTeamName,
      Integer homeGoal,
      Integer awayGoal,
      String gameStatus) {}
}
