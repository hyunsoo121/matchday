package com.heksis.matchday.collector.kleague;

import com.heksis.matchday.collector.kleague.dto.KleagueClubResponse;
import com.heksis.matchday.collector.kleague.dto.KleagueScheduleResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KleagueClient {

  private static final String BASE_URL = "https://www.kleague.com";

  private final RestClient restClient;

  public KleagueClubResponse getClubs(int leagueId, int year) {
    log.info("K리그 팀 조회: leagueId={}, year={}", leagueId, year);
    return restClient
        .post()
        .uri(BASE_URL + "/getClubListByYear.do")
        .header("Content-Type", "application/json")
        .header("Referer", BASE_URL)
        .body(Map.of("leagueId", leagueId, "year", year))
        .retrieve()
        .body(KleagueClubResponse.class);
  }

  public KleagueScheduleResponse getSchedule(int leagueId, int year, int month) {
    log.info("K리그 일정 조회: leagueId={}, year={}, month={}", leagueId, year, month);
    return restClient
        .post()
        .uri(BASE_URL + "/getScheduleList.do")
        .header("Content-Type", "application/json")
        .header("Referer", BASE_URL)
        .body(
            Map.of(
                "leagueId",
                leagueId,
                "year",
                year,
                "month",
                month,
                "teamId",
                "",
                "ticketYn",
                "N",
                "etcYn",
                "N"))
        .retrieve()
        .body(KleagueScheduleResponse.class);
  }
}
