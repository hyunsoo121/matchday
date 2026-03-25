package com.heksis.matchday.collector.kbo;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;

/**
 * KBO 크롤링으로 추출한 경기 데이터.
 *
 * <p>play 텍스트 예시: - 종료: "두산\n5\n:\n3\nLG" - 예정: "두산\n-\n:\n-\nLG" - 취소: "두산\nLG" (비고에 취소 사유)
 */
@Builder
public record KboGameData(
    String date, // "03.22 (토)"
    String time, // "14:00"
    String awayTeam,
    String homeTeam,
    Integer awayScore,
    Integer homeScore,
    String stadium,
    String remark,
    boolean isCancelled) {

  // play 텍스트에서 팀명/점수 추출 패턴
  // "팀A 5 : 3 팀B" 또는 줄바꿈 포함 "팀A\n5\n:\n3\n팀B"
  private static final Pattern SCORE_PATTERN =
      Pattern.compile("^(.+?)\\s+(\\d+)\\s*:\\s*(\\d+)\\s+(.+)$", Pattern.DOTALL);
  private static final Pattern TEAM_ONLY_PATTERN =
      Pattern.compile("^(.+?)\\s+[-]\\s*:\\s*[-]\\s+(.+)$|^(.+?)\\n(.+)$", Pattern.DOTALL);

  public static KboGameData from(Map<String, Object> row) {
    String date = (String) row.getOrDefault("date", "");
    String time = (String) row.getOrDefault("time", "");
    String play = ((String) row.getOrDefault("play", "")).replaceAll("\\s+", " ").trim();
    String stadium = (String) row.getOrDefault("stadium", "");
    String remark = (String) row.getOrDefault("remark", "");

    boolean cancelled = remark.contains("취소") || remark.contains("우천") || remark.contains("콜드");

    // 점수 있는 경우 파싱
    Matcher scoreMatcher = SCORE_PATTERN.matcher(play);
    if (scoreMatcher.matches()) {
      return KboGameData.builder()
          .date(date)
          .time(time)
          .awayTeam(scoreMatcher.group(1).trim())
          .homeTeam(scoreMatcher.group(4).trim())
          .awayScore(Integer.parseInt(scoreMatcher.group(2)))
          .homeScore(Integer.parseInt(scoreMatcher.group(3)))
          .stadium(stadium)
          .remark(remark)
          .isCancelled(false)
          .build();
    }

    // 점수 없는 경우 팀명만 추출
    String[] parts = play.split("\\s*[-:]\\s*|\\s*vs\\s*|\\n");
    String awayTeam = parts.length > 0 ? parts[0].trim() : play;
    String homeTeam = parts.length > 1 ? parts[parts.length - 1].trim() : "";

    return KboGameData.builder()
        .date(date)
        .time(time)
        .awayTeam(awayTeam)
        .homeTeam(homeTeam)
        .awayScore(null)
        .homeScore(null)
        .stadium(stadium)
        .remark(remark)
        .isCancelled(cancelled)
        .build();
  }
}
