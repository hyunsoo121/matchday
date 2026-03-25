package com.heksis.matchday.collector.openf1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenF1DriverDto(
    @JsonProperty("driver_number") int driverNumber,
    @JsonProperty("full_name") String fullName,
    @JsonProperty("name_acronym") String nameAcronym,
    @JsonProperty("team_name") String teamName,
    @JsonProperty("session_key") int sessionKey) {}
