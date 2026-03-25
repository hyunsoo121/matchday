package com.heksis.matchday.collector.kbo;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KboCollectorScheduler {

  private final KboCollector kboCollector;

  // 매 5분 — 오늘 경기 결과/상태 업데이트
  @Scheduled(cron = "0 */5 * * * *")
  public void liveSync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    kboCollector.syncAll(today, today);
  }

  // 매일 새벽 7시 — 어제 결과 보정 + 앞으로 30일 일정 (KBO 시즌: 3월~11월)
  @Scheduled(cron = "0 0 7 * * *")
  public void dailySync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    log.info("KBO 일간 스케줄러 실행: {}", today);
    kboCollector.syncAll(today.minusDays(1), today.plusDays(30));
  }

  // 매주 일요일 새벽 5시 — 연말까지 전체 일정 등록
  @Scheduled(cron = "0 0 5 * * SUN")
  public void weeklyFullSync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
    log.info("KBO 주간 전체 스케줄러 실행: {} ~ {}", today, endOfYear);
    kboCollector.syncAll(today, endOfYear);
  }
}
