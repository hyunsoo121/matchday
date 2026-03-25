package com.heksis.matchday.collector.riot;

import com.heksis.matchday.collector.riot.dto.RiotScheduleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotClient {

  private static final String BASE_URL = "https://esports-api.lolesports.com/persisted/gw";

  private final RestClient restClient;

  @Value("${riot.api-key}")
  private String apiKey;

  public RiotScheduleResponse getSchedule(String leagueId, String pageToken) {
    log.info("Riot 스케줄 조회: leagueId={}, pageToken={}", leagueId, pageToken);
    String url =
        pageToken == null
            ? BASE_URL + "/getSchedule?hl=ko-KR&leagueId=" + leagueId
            : BASE_URL + "/getSchedule?hl=ko-KR&leagueId=" + leagueId + "&pageToken=" + pageToken;
    return restClient
        .get()
        .uri(url)
        .header("x-api-key", apiKey)
        .retrieve()
        .body(RiotScheduleResponse.class);
  }
}
