package com.heksis.matchday.collector.riot;

import com.heksis.matchday.collector.riot.dto.RiotScheduleResponse;
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
public class RiotCollector {

  // Riot eSports API 리그 ID (https://esports-api.lolesports.com/persisted/gw/getLeagues 에서 확인 가능)
  record LeagueConfig(String riotLeagueId, String sportCode, String leagueCode) {}

  private static final List<LeagueConfig> LEAGUE_CONFIGS =
      List.of(
          new LeagueConfig("98767991310872058", "lol", "lck"),
          new LeagueConfig("98767991314006698", "lol", "lpl"),
          new LeagueConfig("98767975604431411", "lol", "lol_worlds"),
          new LeagueConfig("98767991325878492", "lol", "lol_msi"),
          new LeagueConfig("110371976839111107", "lol", "lol_first_stand"),
          new LeagueConfig("107407335260330212", "valorant", "vct"));

  private final RiotClient riotClient;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncAll(LocalDate from, LocalDate to) {
    log.info("Riot 전체 동기화 시작: {} ~ {}", from, to);

    for (LeagueConfig config : LEAGUE_CONFIGS) {
      try {
        Sport sport = sportRepository.findByCode(config.sportCode()).orElseThrow();
        League league = leagueRepository.findByCode(config.leagueCode()).orElseThrow();
        syncLeagueSchedule(config, sport, league, from, to);
      } catch (Exception e) {
        log.error(
            "Riot 리그 동기화 실패: leagueCode={}, error={}", config.leagueCode(), e.getMessage());
      }
    }

    log.info("Riot 전체 동기화 완료: {} ~ {}", from, to);
  }

  private void syncLeagueSchedule(
      LeagueConfig config, Sport sport, League league, LocalDate from, LocalDate to) {
    LocalDateTime fromDt = from.atStartOfDay();
    LocalDateTime toDt = to.atTime(23, 59, 59);

    // 첫 페이지 (현재 기준) 가져오기
    RiotScheduleResponse firstResponse = fetchPage(config, null);
    if (firstResponse == null) return;

    processPage(firstResponse, fromDt, toDt, sport, league, config.sportCode());

    RiotScheduleResponse.Pages pages = firstResponse.data().schedule().pages();
    if (pages == null) return;

    // newer 방향 (미래) 탐색
    String newerToken = pages.newer();
    for (int i = 0; i < 10 && newerToken != null; i++) {
      RiotScheduleResponse response = fetchPage(config, newerToken);
      if (response == null || response.data().schedule().events().isEmpty()) break;

      List<RiotScheduleResponse.Event> events = response.data().schedule().events();
      LocalDateTime firstEventTime = parseDateTime(events.get(0).startTime());
      if (firstEventTime.isAfter(toDt)) break; // 범위 초과, 중단

      processPage(response, fromDt, toDt, sport, league, config.sportCode());
      newerToken = response.data().schedule().pages() != null ? response.data().schedule().pages().newer() : null;
    }

    // older 방향 (과거) 탐색
    String olderToken = pages.older();
    for (int i = 0; i < 10 && olderToken != null; i++) {
      RiotScheduleResponse response = fetchPage(config, olderToken);
      if (response == null || response.data().schedule().events().isEmpty()) break;

      List<RiotScheduleResponse.Event> events = response.data().schedule().events();
      LocalDateTime lastEventTime = parseDateTime(events.get(events.size() - 1).startTime());
      if (lastEventTime.isBefore(fromDt)) break; // 범위 이전, 중단

      processPage(response, fromDt, toDt, sport, league, config.sportCode());
      olderToken = response.data().schedule().pages() != null ? response.data().schedule().pages().older() : null;
    }
  }

  private RiotScheduleResponse fetchPage(LeagueConfig config, String pageToken) {
    try {
      return riotClient.getSchedule(config.riotLeagueId(), pageToken);
    } catch (Exception e) {
      log.warn("Riot 스케줄 조회 실패: leagueCode={}, error={}", config.leagueCode(), e.getMessage());
      return null;
    }
  }

  private void processPage(
      RiotScheduleResponse response,
      LocalDateTime fromDt,
      LocalDateTime toDt,
      Sport sport,
      League league,
      String sportCode) {
    if (response.data() == null
        || response.data().schedule() == null
        || response.data().schedule().events() == null) return;

    for (RiotScheduleResponse.Event event : response.data().schedule().events()) {
      if (!"match".equals(event.type())) continue;

      LocalDateTime eventTime = parseDateTime(event.startTime());
      if (eventTime.isBefore(fromDt) || eventTime.isAfter(toDt)) continue;

      try {
        transactionTemplate.executeWithoutResult(
            status -> syncEvent(sport, league, sportCode, event));
      } catch (Exception e) {
        String matchId = event.match() != null ? event.match().id() : "unknown";
        log.error("Riot 이벤트 동기화 실패: matchId={}, error={}", matchId, e.getMessage());
      }
    }
  }

  private void syncEvent(
      Sport sport, League league, String sportCode, RiotScheduleResponse.Event event) {
    if (event.match() == null || event.match().id() == null) return;

    String externalId = "riot_" + event.match().id();
    LocalDateTime matchTime = parseDateTime(event.startTime());
    MatchStatus status = mapStatus(event.state());

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

    if (event.match().teams() == null) return;

    for (RiotScheduleResponse.Team teamDto : event.match().teams()) {
      if (teamDto.code() == null) continue;

      String teamExternalId = "riot_" + sportCode + "_" + teamDto.code();
      boolean isFinished = status == MatchStatus.FINISHED;

      Team team =
          teamRepository
              .findByExternalId(teamExternalId)
              .map(
                  existing -> {
                    if (teamDto.image() != null) existing.updateLogoUrl(teamDto.image());
                    return existing;
                  })
              .orElseGet(
                  () -> {
                    Team newTeam =
                        Team.create(sport, league, teamDto.name(), teamDto.code(), teamExternalId);
                    if (teamDto.image() != null) newTeam.updateLogoUrl(teamDto.image());
                    return teamRepository.save(newTeam);
                  });

      MatchTeamResult result = calcResult(isFinished, teamDto.result());
      Integer score = teamDto.result() != null ? teamDto.result().gameWins() : null;

      matchTeamRepository
          .findByMatchIdAndTeamId(match.getId(), team.getId())
          .ifPresentOrElse(
              existing -> {
                existing.updateScore(score);
                existing.updateResult(result);
              },
              () -> {
                MatchTeam mt = MatchTeam.create(match, team, MatchTeamRole.PARTICIPANT);
                mt.updateScore(score);
                mt.updateResult(result);
                matchTeamRepository.save(mt);
              });
    }
  }

  private MatchTeamResult calcResult(boolean isFinished, RiotScheduleResponse.Result result) {
    if (!isFinished || result == null) return null;
    return "win".equals(result.outcome()) ? MatchTeamResult.WIN : MatchTeamResult.LOSS;
  }

  private MatchStatus mapStatus(String state) {
    if (state == null) return MatchStatus.SCHEDULED;
    return switch (state) {
      case "inProgress" -> MatchStatus.LIVE;
      case "completed" -> MatchStatus.FINISHED;
      default -> MatchStatus.SCHEDULED;
    };
  }

  private LocalDateTime parseDateTime(String dateStr) {
    if (dateStr == null) return LocalDateTime.now(ZoneOffset.UTC);
    return OffsetDateTime.parse(dateStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }
}
