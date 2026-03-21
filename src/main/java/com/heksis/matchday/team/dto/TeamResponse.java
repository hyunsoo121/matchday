package com.heksis.matchday.team.dto;

import com.heksis.matchday.team.Team;

public record TeamResponse(
    Long id,
    Long sportId,
    Long leagueId,
    String nameKo,
    String nameEn,
    String shortName,
    String logoUrl) {

  public static TeamResponse from(Team team) {
    return new TeamResponse(
        team.getId(),
        team.getSport().getId(),
        team.getLeague() != null ? team.getLeague().getId() : null,
        team.getNameKo(),
        team.getNameEn(),
        team.getShortName(),
        team.getLogoUrl());
  }
}
