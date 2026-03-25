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

  // 매 5분 — 오늘 경기 결과/상태 업데이트 (MLB는 한국 기준 오전~오후 경기)
  @Scheduled(cron = "0 */5 * * * *")
  public void liveSync() {
    LocalDate today = LocalDate.now();
    mlbCollector.syncAll(today, today);
  }

  // 매일 새벽 5시 — 어제 결과 보정 + 앞으로 30일 일정
  @Scheduled(cron = "0 0 5 * * *")
  public void dailySync() {
    LocalDate today = LocalDate.now();
    log.info("MLB 일간 스케줄러 실행: {}", today);
    mlbCollector.syncAll(today.minusDays(1), today.plusDays(30));
  }

  // 매주 일요일 새벽 3시 30분 — 연말까지 전체 일정 등록
  @Scheduled(cron = "0 30 3 * * SUN")
  public void weeklyFullSync() {
    LocalDate today = LocalDate.now();
    LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
    log.info("MLB 주간 전체 스케줄러 실행: {} ~ {}", today, endOfYear);
    mlbCollector.syncAll(today, endOfYear);
  }
}
