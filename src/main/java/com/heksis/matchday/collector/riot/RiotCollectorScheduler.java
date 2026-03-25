package com.heksis.matchday.collector.riot;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotCollectorScheduler {

  private final RiotCollector riotCollector;

  // 매일 새벽 1시 (UTC) = 오전 10시 (KST)
  // LCK/LPL 전날 경기 결과 + 당일 및 7일치 예정 경기 수집
  @Scheduled(cron = "0 0 1 * * *")
  public void scheduledSync() {
    log.info("Riot 스케줄러 실행");
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate nextWeek = LocalDate.now().plusDays(7);
    riotCollector.syncAll(yesterday, nextWeek);
  }
}
