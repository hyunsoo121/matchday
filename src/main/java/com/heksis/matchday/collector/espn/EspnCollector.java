package com.heksis.matchday.collector.espn;

import com.heksis.matchday.collector.espn.dto.EspnScoreboardResponse;
import com.heksis.matchday.collector.espn.dto.EspnTeamsResponse;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EspnCollector {

  record LeagueConfig(String sportPath, String leagueSlug, String sportCode, String leagueCode) {}

  private static final List<LeagueConfig> LEAGUE_CONFIGS =
      List.of(
          new LeagueConfig("soccer", "eng.1", "football", "epl"),
          new LeagueConfig("soccer", "esp.1", "football", "laliga"),
          new LeagueConfig("soccer", "ita.1", "football", "serie_a"),
          new LeagueConfig("soccer", "ger.1", "football", "bundesliga"),
          new LeagueConfig("soccer", "fra.1", "football", "ligue1"),
          new LeagueConfig("soccer", "uefa.champions", "football", "ucl"),
          new LeagueConfig("soccer", "kor.1", "football", "kleague1"),
          new LeagueConfig("soccer", "kor.2", "football", "kleague2"),
          new LeagueConfig("basketball", "nba", "basketball", "nba"));

  private final EspnClient espnClient;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncAll(LocalDate from, LocalDate to) {
    log.info("ESPN 전체 동기화 시작: {} ~ {}", from, to);

    for (LeagueConfig config : LEAGUE_CONFIGS) {
      try {
        Sport sport = sportRepository.findByCode(config.sportCode()).orElseThrow();
        League league = leagueRepository.findByCode(config.leagueCode()).orElseThrow();

        syncLeagueTeams(config, sport, league);

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
          syncLeagueSchedule(config, sport, league, date);
        }
      } catch (Exception e) {
        log.error("ESPN 리그 동기화 실패: leagueCode={}, error={}", config.leagueCode(), e.getMessage());
      }
    }

    log.info("ESPN 전체 동기화 완료: {} ~ {}", from, to);
  }

  private void syncLeagueTeams(LeagueConfig config, Sport sport, League league) {
    EspnTeamsResponse response = espnClient.getTeams(config.sportPath(), config.leagueSlug());
    if (response == null
        || response.sports() == null
        || response.sports().isEmpty()
        || response.sports().get(0).leagues().isEmpty()) {
      log.warn("ESPN 팀 응답 없음: league={}", config.leagueCode());
      return;
    }

    List<EspnTeamsResponse.EspnTeamWrapper> teamWrappers =
        response.sports().get(0).leagues().get(0).teams();

    for (EspnTeamsResponse.EspnTeamWrapper wrapper : teamWrappers) {
      EspnTeamsResponse.EspnTeamDto teamDto = wrapper.team();
      try {
        transactionTemplate.executeWithoutResult(
            status -> {
              String externalId = "espn_" + teamDto.id();
              String shortName =
                  teamDto.abbreviation() != null ? teamDto.abbreviation() : teamDto.displayName();

              teamRepository
                  .findByExternalId(externalId)
                  .ifPresentOrElse(
                      existing -> existing.updateLogoUrl(teamDto.defaultLogoUrl()),
                      () -> {
                        Team team =
                            Team.create(
                                sport, league, teamDto.displayName(), shortName, externalId);
                        team.updateLogoUrl(teamDto.defaultLogoUrl());
                        teamRepository.save(team);
                      });
            });
      } catch (Exception e) {
        log.error("ESPN 팀 동기화 실패: teamId={}, error={}", teamDto.id(), e.getMessage());
      }
    }
  }

  private void syncLeagueSchedule(LeagueConfig config, Sport sport, League league, LocalDate date) {
    EspnScoreboardResponse response;
    try {
      response = espnClient.getScoreboard(config.sportPath(), config.leagueSlug(), date);
    } catch (Exception e) {
      log.warn(
          "ESPN 스코어보드 조회 실패: league={}, date={}, error={}",
          config.leagueCode(),
          date,
          e.getMessage());
      return;
    }
    if (response == null || response.events() == null || response.events().isEmpty()) return;

    for (EspnScoreboardResponse.EspnEventDto event : response.events()) {
      try {
        transactionTemplate.executeWithoutResult(status -> syncEvent(sport, league, event));
      } catch (Exception e) {
        log.error("ESPN 이벤트 동기화 실패: eventId={}, error={}", event.id(), e.getMessage());
      }
    }
  }

  private void syncEvent(Sport sport, League league, EspnScoreboardResponse.EspnEventDto event) {
    String externalId = "espn_" + event.id();
    LocalDateTime matchTime = parseDateTime(event.date());
    MatchStatus status = mapStatus(event.status());

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

    if (event.competitions() == null || event.competitions().isEmpty()) return;

    List<EspnScoreboardResponse.EspnCompetitorDto> competitors =
        event.competitions().get(0).competitors();

    // 양 팀의 winner 여부로 result 계산
    // winner가 한 팀만 true → WIN/LOSS (승부차기 포함)
    // 둘 다 false이고 FINISHED → DRAW
    // FINISHED가 아니면 → null
    boolean isFinished = status == MatchStatus.FINISHED;
    boolean anyWinner =
        competitors.stream().anyMatch(EspnScoreboardResponse.EspnCompetitorDto::winner);

    for (EspnScoreboardResponse.EspnCompetitorDto competitor : competitors) {
      EspnScoreboardResponse.EspnCompetitorTeamDto teamDto = competitor.team();
      String teamExternalId = "espn_" + teamDto.id();

      Team team =
          teamRepository
              .findByExternalId(teamExternalId)
              .orElseGet(
                  () ->
                      teamRepository.save(
                          Team.create(
                              sport,
                              league,
                              teamDto.displayName(),
                              teamDto.displayName(),
                              teamExternalId)));

      MatchTeamRole role =
          "home".equalsIgnoreCase(competitor.homeAway()) ? MatchTeamRole.HOME : MatchTeamRole.AWAY;
      Integer score = parseScore(competitor.score());
      MatchTeamResult result = calcResult(isFinished, anyWinner, competitor.winner());

      matchTeamRepository
          .findByMatchIdAndTeamId(match.getId(), team.getId())
          .ifPresentOrElse(
              existing -> {
                existing.updateScore(score);
                existing.updateResult(result);
              },
              () -> {
                MatchTeam mt = MatchTeam.create(match, team, role);
                mt.updateScore(score);
                mt.updateResult(result);
                matchTeamRepository.save(mt);
              });
    }
  }

  private MatchTeamResult calcResult(boolean isFinished, boolean anyWinner, boolean isWinner) {
    if (!isFinished) return null;
    if (anyWinner) return isWinner ? MatchTeamResult.WIN : MatchTeamResult.LOSS;
    return MatchTeamResult.DRAW;
  }

  private MatchStatus mapStatus(EspnScoreboardResponse.EspnStatusDto statusDto) {
    if (statusDto == null || statusDto.type() == null) return MatchStatus.SCHEDULED;
    EspnScoreboardResponse.EspnStatusTypeDto type = statusDto.type();
    return switch (type.state()) {
      case "in" -> MatchStatus.LIVE;
      case "post" -> type.completed() ? MatchStatus.FINISHED : MatchStatus.POSTPONED;
      default -> MatchStatus.SCHEDULED;
    };
  }

  private Integer parseScore(String score) {
    if (score == null || score.isBlank()) return null;
    try {
      return Integer.parseInt(score.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private LocalDateTime parseDateTime(String dateStr) {
    if (dateStr == null) return LocalDateTime.now(ZoneOffset.UTC);
    return OffsetDateTime.parse(dateStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }
}
