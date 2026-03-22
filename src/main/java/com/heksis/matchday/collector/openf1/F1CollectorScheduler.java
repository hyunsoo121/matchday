package com.heksis.matchday.collector.openf1;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class F1CollectorScheduler {

  private final F1Collector f1Collector;

  @Scheduled(cron = "0 0 3 * * MON")
  public void scheduledSync() {
    int year = LocalDate.now().getYear();
    log.info("F1 스케줄러 실행: year={}", year);
    f1Collector.syncSeason(year);
  }
}
