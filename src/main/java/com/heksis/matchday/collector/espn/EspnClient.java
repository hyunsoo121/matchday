package com.heksis.matchday.collector.espn;

import com.heksis.matchday.collector.espn.dto.EspnScoreboardResponse;
import com.heksis.matchday.collector.espn.dto.EspnTeamsResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class EspnClient {

  private static final String BASE_URL = "https://site.api.espn.com/apis/site/v2/sports";
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final RestClient restClient;

  public EspnTeamsResponse getTeams(String sportPath, String leagueSlug) {
    log.info("ESPN 팀 조회: sport={}, league={}", sportPath, leagueSlug);
    return restClient
        .get()
        .uri(BASE_URL + "/{sport}/{league}/teams", sportPath, leagueSlug)
        .retrieve()
        .body(EspnTeamsResponse.class);
  }

  public EspnScoreboardResponse getScoreboard(String sportPath, String leagueSlug, LocalDate date) {
    String dateStr = date.format(DATE_FORMAT);
    log.info("ESPN 스코어보드 조회: sport={}, league={}, date={}", sportPath, leagueSlug, dateStr);
    return restClient
        .get()
        .uri(BASE_URL + "/{sport}/{league}/scoreboard?dates={date}", sportPath, leagueSlug, dateStr)
        .retrieve()
        .body(EspnScoreboardResponse.class);
  }
}
