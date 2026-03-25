package com.heksis.matchday.collector.kleague.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KleagueClubResponse(String resultCode, Data data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Data(List<Club> clubList) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Club(String teamId, String teamName, String teamNameShort) {}
}
