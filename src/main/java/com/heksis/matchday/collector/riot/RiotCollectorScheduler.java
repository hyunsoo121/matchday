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

  // 매 5분 — 오늘 경기 결과/상태 업데이트 (LCK/LPL은 저녁 경기)
  @Scheduled(cron = "0 */5 * * * *")
  public void liveSync() {
    LocalDate today = LocalDate.now();
    riotCollector.syncAll(today, today);
  }

  // 매일 새벽 1시 (UTC) = 오전 10시 (KST) — 어제 결과 보정 + 앞으로 30일 일정
  @Scheduled(cron = "0 0 1 * * *")
  public void dailySync() {
    LocalDate today = LocalDate.now();
    log.info("Riot 일간 스케줄러 실행: {}", today);
    riotCollector.syncAll(today.minusDays(1), today.plusDays(30));
  }

  // 매주 일요일 새벽 2시 (UTC) — 연말까지 전체 일정 등록 (새 스플릿 일정 포함)
  @Scheduled(cron = "0 0 2 * * SUN")
  public void weeklyFullSync() {
    LocalDate today = LocalDate.now();
    LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
    log.info("Riot 주간 전체 스케줄러 실행: {} ~ {}", today, endOfYear);
    riotCollector.syncAll(today, endOfYear);
  }
}
