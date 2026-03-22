package com.heksis.matchday.collector.mlb.dto;

import java.util.List;

public record MlbScheduleResponse(List<MlbDateDto> dates) {

  public record MlbDateDto(List<MlbGameDto> games) {}

  public record MlbGameDto(
      long gamePk, String gameDate, MlbGameStatusDto status, MlbGameTeamsDto teams) {}

  public record MlbGameStatusDto(String abstractGameState, String detailedState) {}

  public record MlbGameTeamsDto(MlbGameTeamDto home, MlbGameTeamDto away) {}

  public record MlbGameTeamDto(MlbTeamRefDto team, Integer score, Boolean isWinner) {}

  public record MlbTeamRefDto(int id, String name) {}
}
