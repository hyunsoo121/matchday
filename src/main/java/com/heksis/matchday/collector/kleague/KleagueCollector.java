package com.heksis.matchday.collector.kleague;

import com.heksis.matchday.collector.kleague.dto.KleagueClubResponse;
import com.heksis.matchday.collector.kleague.dto.KleagueScheduleResponse;
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
public class KleagueCollector {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

  record LeagueConfig(int kleagueId, String leagueCode) {}

  private static final List<LeagueConfig> LEAGUE_CONFIGS =
      List.of(new LeagueConfig(1, "kleague1"), new LeagueConfig(2, "kleague2"));

  private final KleagueClient kleagueClient;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncAll(LocalDate from, LocalDate to) {
    log.info("K리그 전체 동기화 시작: {} ~ {}", from, to);

    Sport sport = sportRepository.findByCode("football").orElseThrow();

    for (LeagueConfig config : LEAGUE_CONFIGS) {
      try {
        League league = leagueRepository.findByCode(config.leagueCode()).orElseThrow();
        syncTeams(config, sport, league, from.getYear());
        syncSchedule(config, sport, league, from, to);
      } catch (Exception e) {
        log.error("K리그 동기화 실패: leagueCode={}, error={}", config.leagueCode(), e.getMessage());
      }
    }

    log.info("K리그 전체 동기화 완료: {} ~ {}", from, to);
  }

  private void syncTeams(LeagueConfig config, Sport sport, League league, int year) {
    KleagueClubResponse response;
    try {
      response = kleagueClient.getClubs(config.kleagueId(), year);
    } catch (Exception e) {
      log.warn("K리그 팀 조회 실패: leagueCode={}, error={}", config.leagueCode(), e.getMessage());
      return;
    }

    if (response == null || response.data() == null || response.data().clubList() == null) return;

    for (KleagueClubResponse.Club club : response.data().clubList()) {
      try {
        transactionTemplate.executeWithoutResult(
            status -> {
              String externalId = "kleague_" + club.teamId();
              teamRepository
                  .findByExternalId(externalId)
                  .orElseGet(
                      () ->
                          teamRepository.save(
                              Team.create(
                                  sport,
                                  league,
                                  club.teamName(),
                                  club.teamNameShort(),
                                  externalId)));
            });
      } catch (Exception e) {
        log.error("K리그 팀 동기화 실패: teamId={}, error={}", club.teamId(), e.getMessage());
      }
    }
  }

  private void syncSchedule(
      LeagueConfig config, Sport sport, League league, LocalDate from, LocalDate to) {
    // 월 단위 API이므로 from~to 범위의 월을 순회
    YearMonth startMonth = YearMonth.from(from);
    YearMonth endMonth = YearMonth.from(to);

    for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
      KleagueScheduleResponse response;
      try {
        response = kleagueClient.getSchedule(config.kleagueId(), ym.getYear(), ym.getMonthValue());
      } catch (Exception e) {
        log.warn("K리그 일정 조회 실패: {}-{}, error={}", ym.getYear(), ym.getMonthValue(), e.getMessage());
        continue;
      }

      if (response == null || response.data() == null || response.data().scheduleList() == null)
        continue;

      for (KleagueScheduleResponse.Game game : response.data().scheduleList()) {
        try {
          transactionTemplate.executeWithoutResult(status -> syncGame(sport, league, game));
        } catch (Exception e) {
          log.error("K리그 경기 동기화 실패: gameId={}, error={}", game.gameId(), e.getMessage());
        }
      }
    }
  }

  private void syncGame(Sport sport, League league, KleagueScheduleResponse.Game game) {
    String externalId = "kleague_" + game.year() + "_" + game.gameId();
    LocalDateTime matchTimeUtc = parseToUtc(game.gameDate(), game.gameTime());
    MatchStatus status = mapStatus(game.gameStatus());

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
                        Match.create(sport, league, matchTimeUtc, status, externalId, null)));

    Team homeTeam = findOrCreateTeam(sport, league, game.homeTeam(), game.homeTeamName());
    Team awayTeam = findOrCreateTeam(sport, league, game.awayTeam(), game.awayTeamName());

    boolean isFinished = status == MatchStatus.FINISHED;

    syncMatchTeam(
        match, homeTeam, MatchTeamRole.HOME, game.homeGoal(), game.awayGoal(), isFinished);
    syncMatchTeam(
        match, awayTeam, MatchTeamRole.AWAY, game.awayGoal(), game.homeGoal(), isFinished);
  }

  private void syncMatchTeam(
      Match match,
      Team team,
      MatchTeamRole role,
      Integer myGoal,
      Integer opponentGoal,
      boolean isFinished) {
    MatchTeamResult result = calcResult(isFinished, myGoal, opponentGoal);

    matchTeamRepository
        .findByMatchIdAndTeamId(match.getId(), team.getId())
        .ifPresentOrElse(
            existing -> {
              existing.updateScore(myGoal);
              existing.updateResult(result);
            },
            () -> {
              MatchTeam mt = MatchTeam.create(match, team, role);
              mt.updateScore(myGoal);
              mt.updateResult(result);
              matchTeamRepository.save(mt);
            });
  }

  private Team findOrCreateTeam(Sport sport, League league, String teamId, String teamName) {
    String externalId = "kleague_" + teamId;
    return teamRepository
        .findByExternalId(externalId)
        .orElseGet(
            () -> teamRepository.save(Team.create(sport, league, teamName, teamName, externalId)));
  }

  private MatchTeamResult calcResult(boolean isFinished, Integer myGoal, Integer opponentGoal) {
    if (!isFinished || myGoal == null || opponentGoal == null) return null;
    if (myGoal > opponentGoal) return MatchTeamResult.WIN;
    if (myGoal < opponentGoal) return MatchTeamResult.LOSS;
    return MatchTeamResult.DRAW;
  }

  private MatchStatus mapStatus(String gameStatus) {
    if (gameStatus == null || gameStatus.isBlank()) return MatchStatus.SCHEDULED;
    return switch (gameStatus) {
      case "FE" -> MatchStatus.FINISHED;
      case "CANCEL", "POSTPONED" -> MatchStatus.POSTPONED;
      default -> MatchStatus.LIVE;
    };
  }

  private LocalDateTime parseToUtc(String gameDate, String gameTime) {
    try {
      String[] timeParts = (gameTime != null ? gameTime : "00:00").split(":");
      LocalDate date = LocalDate.parse(gameDate, DATE_FORMAT);
      ZonedDateTime kst =
          date.atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])).atZone(KST);
      return kst.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    } catch (Exception e) {
      return LocalDateTime.now(ZoneOffset.UTC);
    }
  }
}
