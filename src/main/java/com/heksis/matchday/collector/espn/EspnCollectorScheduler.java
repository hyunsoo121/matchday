package com.heksis.matchday.collector.espn;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EspnCollectorScheduler {

  private final EspnCollector espnCollector;

  // 매일 새벽 4시 실행 — 어제 결과 + 오늘 + 앞으로 7일 경기 동기화
  @Scheduled(cron = "0 0 4 * * *")
  public void scheduledSync() {
    LocalDate today = LocalDate.now();
    log.info("ESPN 스케줄러 실행: {}", today);
    espnCollector.syncAll(today.minusDays(1), today.plusDays(7));
  }
}
