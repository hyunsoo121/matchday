package com.heksis.matchday.collector.espn.dto;

import java.util.List;

public record EspnTeamsResponse(List<EspnSportDto> sports) {

  public record EspnSportDto(List<EspnLeagueTeamsDto> leagues) {}

  public record EspnLeagueTeamsDto(List<EspnTeamWrapper> teams) {}

  public record EspnTeamWrapper(EspnTeamDto team) {}

  public record EspnTeamDto(
      String id,
      String displayName,
      String shortDisplayName,
      String abbreviation,
      boolean isActive,
      List<EspnLogoDto> logos) {

    public String defaultLogoUrl() {
      if (logos == null || logos.isEmpty()) return null;
      return logos.stream()
          .filter(l -> l.rel() != null && l.rel().contains("default"))
          .findFirst()
          .map(EspnLogoDto::href)
          .orElse(logos.get(0).href());
    }
  }

  public record EspnLogoDto(String href, List<String> rel) {}
}
