package com.heksis.matchday.collector.mlb.dto;

import java.util.List;

public record MlbTeamsResponse(List<MlbTeamDto> teams) {

  public record MlbTeamDto(
      int id, String name, String abbreviation, String shortName, boolean active) {}
}
