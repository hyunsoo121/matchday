package com.heksis.matchday.collector.mlb;

import com.heksis.matchday.collector.mlb.dto.MlbScheduleResponse;
import com.heksis.matchday.collector.mlb.dto.MlbTeamsResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MlbClient {

  private static final String BASE_URL = "https://statsapi.mlb.com/api/v1";
  private static final int SPORT_ID = 1; // MLB

  private final RestClient restClient;

  public MlbTeamsResponse getTeams(int season) {
    log.info("MLB 팀 조회: season={}", season);
    return restClient
        .get()
        .uri(BASE_URL + "/teams?sportId={sportId}&season={season}", SPORT_ID, season)
        .retrieve()
        .body(MlbTeamsResponse.class);
  }

  public MlbScheduleResponse getSchedule(LocalDate date) {
    log.info("MLB 스케줄 조회: date={}", date);
    return restClient
        .get()
        .uri(BASE_URL + "/schedule?sportId={sportId}&date={date}", SPORT_ID, date)
        .retrieve()
        .body(MlbScheduleResponse.class);
  }
}
