package com.heksis.matchday.collector.mlb;

import com.heksis.matchday.global.dto.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collector/mlb")
@RequiredArgsConstructor
public class MlbCollectorController {

  private final MlbCollector mlbCollector;

  @PostMapping("/sync")
  public ApiResponse<String> sync(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    LocalDate today = LocalDate.now();
    LocalDate syncFrom = from != null ? from : today.minusDays(1);
    LocalDate syncTo = to != null ? to : today.plusDays(7);
    mlbCollector.syncAll(syncFrom, syncTo);
    return ApiResponse.ok("MLB 동기화 완료: " + syncFrom + " ~ " + syncTo);
  }
}
