package com.heksis.matchday.collector.riot;

import com.heksis.matchday.global.dto.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collector/riot")
@RequiredArgsConstructor
public class RiotCollectorController {

  private final RiotCollector riotCollector;

  @PostMapping("/sync")
  public ApiResponse<String> sync(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    LocalDate syncFrom = from != null ? from : LocalDate.now().minusDays(1);
    LocalDate syncTo = to != null ? to : LocalDate.now().plusDays(7);
    riotCollector.syncAll(syncFrom, syncTo);
    return ApiResponse.ok("Riot 동기화 완료: %s ~ %s".formatted(syncFrom, syncTo));
  }
}
