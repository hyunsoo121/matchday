package com.heksis.matchday.collector.kbo;

import com.heksis.matchday.league.League;
import com.heksis.matchday.league.LeagueRepository;
import com.heksis.matchday.match.Match;
import com.heksis.matchday.match.MatchRepository;
import com.heksis.matchday.match.MatchStatus;
import com.heksis.matchday.match.MatchTeam;
import com.heksis.matchday.match.MatchTeamRepository;
import com.heksis.matchday.match.MatchTeamResult;
import com.heksis.matchday.match.MatchTeamRole;
import com.heksis.matchday.sport.Sport;
import com.heksis.matchday.sport.SportRepository;
import com.heksis.matchday.team.Team;
import com.heksis.matchday.team.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KboCollector {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  // KBO 날짜 포맷: "03.22 (토)" → MM.dd 부분만 파싱
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM.dd");

  private final KboCrawler kboCrawler;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncAll(LocalDate from, LocalDate to) {
    log.info("KBO 전체 동기화 시작: {} ~ {}", from, to);

    Sport sport = sportRepository.findByCode("baseball").orElseThrow();
    League league = leagueRepository.findByCode("kbo").orElseThrow();

    YearMonth startMonth = YearMonth.from(from);
    YearMonth endMonth = YearMonth.from(to);

    for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
      List<KboGameData> games = kboCrawler.crawl(ym.getYear(), ym.getMonthValue());

      for (KboGameData game : games) {
        if (game.awayTeam().isBlank() || game.homeTeam().isBlank()) continue;

        LocalDateTime matchTime = parseToUtc(game.date(), game.time(), ym.getYear());
        if (matchTime == null) continue;

        // from~to 범위 밖이면 스킵
        LocalDate matchDate =
            matchTime.atZone(ZoneOffset.UTC).withZoneSameInstant(KST).toLocalDate();
        if (matchDate.isBefore(from) || matchDate.isAfter(to)) continue;

        try {
          transactionTemplate.executeWithoutResult(
              status -> syncGame(sport, league, game, matchTime));
        } catch (Exception e) {
          log.error(
              "KBO 경기 동기화 실패: {}-{} {}, error={}",
              game.date(),
              game.awayTeam(),
              game.homeTeam(),
              e.getMessage());
        }
      }
    }

    log.info("KBO 전체 동기화 완료: {} ~ {}", from, to);
  }

  private void syncGame(Sport sport, League league, KboGameData game, LocalDateTime matchTime) {
    String externalId = buildExternalId(game, matchTime);
    MatchStatus status = mapStatus(game);

    Match match =
        matchRepository
            .findByExternalId(externalId)
            .map(
                existing -> {
                  existing.updateStatus(status);
                  return existing;
                })
            .orElseGet(
                () ->
                    matchRepository.save(
                        Match.create(sport, league, matchTime, status, externalId, null)));

    Team awayTeam = findOrCreateTeam(sport, league, game.awayTeam());
    Team homeTeam = findOrCreateTeam(sport, league, game.homeTeam());
    boolean isFinished = status == MatchStatus.FINISHED;

    syncMatchTeam(
        match, awayTeam, MatchTeamRole.AWAY, game.awayScore(), game.homeScore(), isFinished);
    syncMatchTeam(
        match, homeTeam, MatchTeamRole.HOME, game.homeScore(), game.awayScore(), isFinished);
  }

  private void syncMatchTeam(
      Match match,
      Team team,
      MatchTeamRole role,
      Integer myScore,
      Integer opponentScore,
      boolean isFinished) {
    MatchTeamResult result = calcResult(isFinished, myScore, opponentScore);

    matchTeamRepository
        .findByMatchIdAndTeamId(match.getId(), team.getId())
        .ifPresentOrElse(
            existing -> {
              existing.updateScore(myScore);
              existing.updateResult(result);
            },
            () -> {
              MatchTeam mt = MatchTeam.create(match, team, role);
              mt.updateScore(myScore);
              mt.updateResult(result);
              matchTeamRepository.save(mt);
            });
  }

  private Team findOrCreateTeam(Sport sport, League league, String teamName) {
    String externalId = "kbo_" + teamName;
    return teamRepository
        .findByExternalId(externalId)
        .orElseGet(
            () -> teamRepository.save(Team.create(sport, league, teamName, teamName, externalId)));
  }

  private MatchTeamResult calcResult(boolean isFinished, Integer myScore, Integer opponentScore) {
    if (!isFinished || myScore == null || opponentScore == null) return null;
    if (myScore > opponentScore) return MatchTeamResult.WIN;
    if (myScore < opponentScore) return MatchTeamResult.LOSS;
    return MatchTeamResult.DRAW;
  }

  private MatchStatus mapStatus(KboGameData game) {
    if (game.isCancelled()) return MatchStatus.POSTPONED;
    if (game.awayScore() != null && game.homeScore() != null) return MatchStatus.FINISHED;
    return MatchStatus.SCHEDULED;
  }

  // externalId: "kbo_{year}_{mmdd}_{awayTeam}_{homeTeam}"
  private String buildExternalId(KboGameData game, LocalDateTime matchTime) {
    String mmdd =
        String.format(
            "%02d%02d",
            matchTime.atZone(ZoneOffset.UTC).withZoneSameInstant(KST).getMonthValue(),
            matchTime.atZone(ZoneOffset.UTC).withZoneSameInstant(KST).getDayOfMonth());
    return "kbo_"
        + matchTime.getYear()
        + "_"
        + mmdd
        + "_"
        + game.awayTeam()
        + "_"
        + game.homeTeam();
  }

  private LocalDateTime parseToUtc(String dateStr, String timeStr, int year) {
    try {
      // "03.22 (토)" → "03.22"
      String mmdd = dateStr.replaceAll("\\(.*?\\)", "").trim();
      LocalDate date = MonthDay.parse(mmdd, DATE_FORMAT).atYear(year);
      String[] timeParts = (timeStr != null && !timeStr.isBlank() ? timeStr : "00:00").split(":");
      ZonedDateTime kst =
          date.atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])).atZone(KST);
      return kst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    } catch (Exception e) {
      log.warn("KBO 날짜 파싱 실패: date={}, time={}", dateStr, timeStr);
      return null;
    }
  }
}
