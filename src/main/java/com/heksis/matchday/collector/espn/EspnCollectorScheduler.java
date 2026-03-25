package com.heksis.matchday.collector.espn;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EspnCollectorScheduler {

  private final EspnCollector espnCollector;

  // 매 5분 — 오늘 경기 결과/상태 업데이트 (LIVE, FINISHED)
  @Scheduled(cron = "0 */5 * * * *")
  public void liveSync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    espnCollector.syncAll(today, today);
  }

  // 매일 새벽 4시 — 어제 결과 보정 + 앞으로 30일 일정
  @Scheduled(cron = "0 0 4 * * *")
  public void dailySync() {
    LocalDate today = LocalDate.now();
    log.info("ESPN 일간 스케줄러 실행: {}", today);
    espnCollector.syncAll(today.minusDays(1), today.plusDays(30));
  }

  // 매주 일요일 새벽 3시 — 연말까지 전체 일정 등록 (새로 공개된 경기 포함)
  @Scheduled(cron = "0 0 3 * * SUN")
  public void weeklyFullSync() {
    LocalDate today = LocalDate.now();
    LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
    log.info("ESPN 주간 전체 스케줄러 실행: {} ~ {}", today, endOfYear);
    espnCollector.syncAll(today, endOfYear);
  }
}
