package com.heksis.matchday.collector.mlb;

import com.heksis.matchday.collector.mlb.dto.MlbScheduleResponse;
import com.heksis.matchday.collector.mlb.dto.MlbTeamsResponse;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlbCollector {

  private final MlbClient mlbClient;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncAll(LocalDate from, LocalDate to) {
    log.info("MLB 전체 동기화 시작: {} ~ {}", from, to);

    Sport sport = sportRepository.findByCode("baseball").orElseThrow();
    League league = leagueRepository.findByCode("mlb").orElseThrow();

    syncTeams(sport, league, from.getYear());

    for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
      syncSchedule(sport, league, date);
    }

    log.info("MLB 전체 동기화 완료: {} ~ {}", from, to);
  }

  private void syncTeams(Sport sport, League league, int season) {
    MlbTeamsResponse response = mlbClient.getTeams(season);
    if (response == null || response.teams() == null) return;

    for (MlbTeamsResponse.MlbTeamDto teamDto : response.teams()) {
      if (!teamDto.active()) continue;
      try {
        transactionTemplate.executeWithoutResult(
            status -> {
              String externalId = "mlb_" + teamDto.id();
              String logoUrl = "https://www.mlbstatic.com/team-logos/" + teamDto.id() + ".svg";

              teamRepository
                  .findByExternalId(externalId)
                  .ifPresentOrElse(
                      existing -> existing.updateLogoUrl(logoUrl),
                      () -> {
                        Team team =
                            Team.create(
                                sport, league, teamDto.name(), teamDto.abbreviation(), externalId);
                        team.updateLogoUrl(logoUrl);
                        teamRepository.save(team);
                      });
            });
      } catch (Exception e) {
        log.error("MLB 팀 동기화 실패: teamId={}, error={}", teamDto.id(), e.getMessage());
      }
    }
  }

  private void syncSchedule(Sport sport, League league, LocalDate date) {
    MlbScheduleResponse response;
    try {
      response = mlbClient.getSchedule(date);
    } catch (Exception e) {
      log.warn("MLB 스케줄 조회 실패: date={}, error={}", date, e.getMessage());
      return;
    }
    if (response == null || response.dates() == null || response.dates().isEmpty()) return;

    for (MlbScheduleResponse.MlbDateDto dateDto : response.dates()) {
      if (dateDto.games() == null) continue;
      for (MlbScheduleResponse.MlbGameDto game : dateDto.games()) {
        try {
          transactionTemplate.executeWithoutResult(status -> syncGame(sport, league, game));
        } catch (Exception e) {
          log.error("MLB 경기 동기화 실패: gamePk={}, error={}", game.gamePk(), e.getMessage());
        }
      }
    }
  }

  private void syncGame(Sport sport, League league, MlbScheduleResponse.MlbGameDto game) {
    String externalId = "mlb_" + game.gamePk();
    LocalDateTime matchTime = parseDateTime(game.gameDate());
    MatchStatus status = mapStatus(game.status());

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

    syncGameTeam(match, sport, league, game.teams().home(), MatchTeamRole.HOME, status);
    syncGameTeam(match, sport, league, game.teams().away(), MatchTeamRole.AWAY, status);
  }

  private void syncGameTeam(
      Match match,
      Sport sport,
      League league,
      MlbScheduleResponse.MlbGameTeamDto gameTeam,
      MatchTeamRole role,
      MatchStatus matchStatus) {
    String teamExternalId = "mlb_" + gameTeam.team().id();

    Team team =
        teamRepository
            .findByExternalId(teamExternalId)
            .orElseGet(
                () ->
                    teamRepository.save(
                        Team.create(
                            sport,
                            league,
                            gameTeam.team().name(),
                            gameTeam.team().name(),
                            teamExternalId)));

    boolean isFinished = matchStatus == MatchStatus.FINISHED;
    MatchTeamResult result = calcResult(isFinished, gameTeam.isWinner());

    matchTeamRepository
        .findByMatchIdAndTeamId(match.getId(), team.getId())
        .ifPresentOrElse(
            existing -> {
              existing.updateScore(gameTeam.score());
              existing.updateResult(result);
            },
            () -> {
              MatchTeam mt = MatchTeam.create(match, team, role);
              mt.updateScore(gameTeam.score());
              mt.updateResult(result);
              matchTeamRepository.save(mt);
            });
  }

  private MatchTeamResult calcResult(boolean isFinished, Boolean isWinner) {
    if (!isFinished || isWinner == null) return null;
    return isWinner ? MatchTeamResult.WIN : MatchTeamResult.LOSS;
  }

  private MatchStatus mapStatus(MlbScheduleResponse.MlbGameStatusDto status) {
    if (status == null) return MatchStatus.SCHEDULED;
    if ("Postponed".equals(status.detailedState())) return MatchStatus.POSTPONED;
    return switch (status.abstractGameState()) {
      case "Live" -> MatchStatus.LIVE;
      case "Final" -> MatchStatus.FINISHED;
      default -> MatchStatus.SCHEDULED;
    };
  }

  private LocalDateTime parseDateTime(String dateStr) {
    if (dateStr == null) return LocalDateTime.now(ZoneOffset.UTC);
    return OffsetDateTime.parse(dateStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }
}
