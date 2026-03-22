package com.heksis.matchday.collector.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1SessionDto(
    @JsonProperty("session_key") int sessionKey,
    @JsonProperty("session_name") String sessionName,
    @JsonProperty("date_start") String dateStart,
    @JsonProperty("date_end") String dateEnd,
    @JsonProperty("meeting_key") int meetingKey,
    @JsonProperty("meeting_name") String meetingName,
    @JsonProperty("location") String location,
    @JsonProperty("country_name") String countryName,
    @JsonProperty("year") int year) {}
