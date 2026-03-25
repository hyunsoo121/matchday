package com.heksis.matchday.collector.espn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EspnScoreboardResponse(List<EspnEventDto> events) {

  public record EspnEventDto(
      String id, String date, EspnStatusDto status, List<EspnCompetitionDto> competitions) {}

  public record EspnStatusDto(@JsonProperty("type") EspnStatusTypeDto type) {}

  public record EspnStatusTypeDto(String state, boolean completed) {}

  public record EspnCompetitionDto(List<EspnCompetitorDto> competitors) {}

  public record EspnCompetitorDto(
      String homeAway, String score, boolean winner, EspnCompetitorTeamDto team) {}

  public record EspnCompetitorTeamDto(String id, String displayName, String logo) {}
}
