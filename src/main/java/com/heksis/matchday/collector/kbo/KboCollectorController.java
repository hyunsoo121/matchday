package com.heksis.matchday.collector.kbo;

import com.heksis.matchday.global.dto.ApiResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collector/kbo")
@RequiredArgsConstructor
public class KboCollectorController {

  private final KboCollector kboCollector;

  @PostMapping("/sync")
  public ApiResponse<String> sync(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDate syncFrom = from != null ? from : today.minusDays(1);
    LocalDate syncTo = to != null ? to : today.plusDays(30);
    kboCollector.syncAll(syncFrom, syncTo);
    return ApiResponse.ok("KBO 동기화 완료: " + syncFrom + " ~ " + syncTo);
  }
}
