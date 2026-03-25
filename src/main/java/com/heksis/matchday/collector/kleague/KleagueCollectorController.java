package com.heksis.matchday.collector.kleague;

import com.heksis.matchday.global.dto.ApiResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collector/kleague")
@RequiredArgsConstructor
public class KleagueCollectorController {

  private final KleagueCollector kleagueCollector;

  @PostMapping("/sync")
  public ApiResponse<String> sync(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    LocalDate syncFrom = from != null ? from : LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
    LocalDate syncTo = to != null ? to : LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(30);
    kleagueCollector.syncAll(syncFrom, syncTo);
    return ApiResponse.ok("K리그 동기화 완료: %s ~ %s".formatted(syncFrom, syncTo));
  }
}
