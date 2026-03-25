package com.heksis.matchday.collector.riot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiotScheduleResponse(Data data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Data(Schedule schedule) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Schedule(Pages pages, List<Event> events) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Pages(String older, String newer) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Event(String startTime, String state, String type, Match match) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Match(String id, List<Team> teams) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Team(String name, String code, String image, Result result) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Result(String outcome, Integer gameWins) {}
}
