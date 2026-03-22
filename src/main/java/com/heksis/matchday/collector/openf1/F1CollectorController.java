package com.heksis.matchday.collector.openf1;

import com.heksis.matchday.global.dto.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collector/f1")
@RequiredArgsConstructor
public class F1CollectorController {

  private final F1Collector f1Collector;

  @PostMapping("/sync")
  public ApiResponse<String> sync(@RequestParam(defaultValue = "0") int year) {
    int targetYear = year == 0 ? LocalDate.now().getYear() : year;
    f1Collector.syncSeason(targetYear);
    return ApiResponse.ok("F1 시즌 동기화 완료: " + targetYear);
  }
}
