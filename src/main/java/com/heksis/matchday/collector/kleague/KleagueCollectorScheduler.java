package com.heksis.matchday.collector.kleague;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KleagueCollectorScheduler {

  private final KleagueCollector kleagueCollector;

  // 매 5분 — 오늘 경기 결과/상태 업데이트
  @Scheduled(cron = "0 */5 * * * *")
  public void liveSync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    kleagueCollector.syncAll(today, today);
  }

  // 매일 새벽 6시 — 어제 결과 보정 + 앞으로 30일 일정
  @Scheduled(cron = "0 0 6 * * *")
  public void dailySync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    log.info("K리그 일간 스케줄러 실행: {}", today);
    kleagueCollector.syncAll(today.minusDays(1), today.plusDays(30));
  }

  // 매주 일요일 새벽 4시 — 연말까지 전체 일정 등록
  @Scheduled(cron = "0 0 4 * * SUN")
  public void weeklyFullSync() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());
    log.info("K리그 주간 전체 스케줄러 실행: {} ~ {}", today, endOfYear);
    kleagueCollector.syncAll(today, endOfYear);
  }
}
