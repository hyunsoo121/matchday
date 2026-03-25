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

  // 매일 새벽 3시 — 시즌 전체 세션 sync (레이스 결과 + 신규 일정)
  // F1은 시즌 단위로 한 번에 가져오는 구조라 daily로 충분
  @Scheduled(cron = "0 0 3 * * *")
  public void dailySync() {
    int year = LocalDate.now().getYear();
    log.info("F1 스케줄러 실행: year={}", year);
    f1Collector.syncSeason(year);
  }
}
