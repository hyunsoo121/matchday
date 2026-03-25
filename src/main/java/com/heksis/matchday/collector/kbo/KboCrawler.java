package com.heksis.matchday.collector.kbo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KboCrawler {

  private static final String SCHEDULE_URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

  // 정규시즌 + 포스트시즌
  private static final String SERIES_ID = "0,9,6";

  @SuppressWarnings("unchecked")
  public List<KboGameData> crawl(int year, int month) {
    log.info("KBO 크롤링 시작: year={}, month={}", year, month);

    try (Playwright playwright = Playwright.create()) {
      try (Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {

        Page page = browser.newPage();
        page.navigate(SCHEDULE_URL);
        page.waitForLoadState();

        // 연도 선택
        page.selectOption("#ddlYear", String.valueOf(year));
        // 월 선택 (두 자리 포맷)
        page.selectOption("#ddlMonth", String.format("%02d", month));
        // 시리즈 선택 (정규시즌 + 포스트시즌)
        page.selectOption("#ddlSeries", SERIES_ID);

        // 테이블 로딩 대기
        page.waitForTimeout(1500);

        // JavaScript로 테이블 데이터 추출
        List<Map<String, Object>> rows =
            (List<Map<String, Object>>)
                page.evaluate(
                    """
                    () => {
                      const rows = document.querySelectorAll('#tblScheduleList tbody tr');
                      const result = [];
                      let currentDate = '';

                      rows.forEach(row => {
                        const dayEl = row.querySelector('td.day');
                        if (dayEl && dayEl.innerText.trim()) {
                          currentDate = dayEl.innerText.trim();
                        }

                        const timeEl = row.querySelector('td.time');
                        const playEl = row.querySelector('td.play');
                        if (!timeEl || !playEl) return;

                        const tds = row.querySelectorAll('td');
                        const stadium = tds[tds.length - 2]?.innerText.trim() || '';
                        const remark  = tds[tds.length - 1]?.innerText.trim() || '';

                        result.push({
                          date:    currentDate,
                          time:    timeEl.innerText.trim(),
                          play:    playEl.innerText.trim(),
                          stadium: stadium,
                          remark:  remark
                        });
                      });

                      return result;
                    }
                    """);

        log.info("KBO 크롤링 완료: year={}, month={}, rows={}", year, month, rows.size());
        return rows.stream().map(KboGameData::from).toList();
      }
    } catch (Exception e) {
      log.error("KBO 크롤링 실패: year={}, month={}, error={}", year, month, e.getMessage());
      return List.of();
    }
  }
}
