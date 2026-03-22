package com.heksis.matchday.collector.openf1;

import com.heksis.matchday.collector.openf1.dto.OpenF1DriverDto;
import com.heksis.matchday.collector.openf1.dto.OpenF1SessionDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenF1Client {

  private static final String BASE_URL = "https://api.openf1.org/v1";

  private final RestClient restClient;

  public List<OpenF1SessionDto> getRaceSessions(int year) {
    log.info("OpenF1 레이스 세션 조회: year={}", year);
    return restClient
        .get()
        .uri(BASE_URL + "/sessions?year={year}&session_name=Race", year)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  public List<OpenF1DriverDto> getDrivers(int sessionKey) {
    log.info("OpenF1 드라이버 조회: sessionKey={}", sessionKey);
    return restClient
        .get()
        .uri(BASE_URL + "/drivers?session_key={sessionKey}", sessionKey)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
