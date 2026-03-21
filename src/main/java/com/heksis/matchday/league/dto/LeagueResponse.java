package com.heksis.matchday.league.dto;

import com.heksis.matchday.league.League;

public record LeagueResponse(
    Long id,
    Long sportId,
    String code,
    String nameKo,
    String nameEn,
    String country,
    String logoUrl,
    int displayOrder) {

  public static LeagueResponse from(League league) {
    return new LeagueResponse(
        league.getId(),
        league.getSport().getId(),
        league.getCode(),
        league.getNameKo(),
        league.getNameEn(),
        league.getCountry(),
        league.getLogoUrl(),
        league.getDisplayOrder());
  }
}
