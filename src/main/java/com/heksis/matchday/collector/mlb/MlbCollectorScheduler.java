package com.heksis.matchday.collector.mlb;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MlbCollectorScheduler {

  private final MlbCollector mlbCollector;

  // 매일 새벽 5시 실행 (MLB는 한국 기준 낮/저녁 경기)
  @Scheduled(cron = "0 0 5 * * *")
  public void scheduledSync() {
    LocalDate today = LocalDate.now();
    log.info("MLB 스케줄러 실행: {}", today);
    mlbCollector.syncAll(today.minusDays(1), today.plusDays(7));
  }
}
