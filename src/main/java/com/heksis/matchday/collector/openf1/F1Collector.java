package com.heksis.matchday.collector.openf1;

import com.heksis.matchday.collector.openf1.dto.OpenF1DriverDto;
import com.heksis.matchday.collector.openf1.dto.OpenF1SessionDto;
import com.heksis.matchday.league.League;
import com.heksis.matchday.league.LeagueRepository;
import com.heksis.matchday.match.Match;
import com.heksis.matchday.match.MatchRepository;
import com.heksis.matchday.match.MatchStatus;
import com.heksis.matchday.match.MatchTeam;
import com.heksis.matchday.match.MatchTeamRepository;
import com.heksis.matchday.match.MatchTeamRole;
import com.heksis.matchday.sport.Sport;
import com.heksis.matchday.sport.SportRepository;
import com.heksis.matchday.team.Team;
import com.heksis.matchday.team.TeamRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class F1Collector {

  private final OpenF1Client openF1Client;
  private final SportRepository sportRepository;
  private final LeagueRepository leagueRepository;
  private final TeamRepository teamRepository;
  private final MatchRepository matchRepository;
  private final MatchTeamRepository matchTeamRepository;
  private final TransactionTemplate transactionTemplate;

  public void syncSeason(int year) {
    log.info("F1 시즌 동기화 시작: year={}", year);

    Sport sport = sportRepository.findByCode("f1").orElseThrow();
    League league = leagueRepository.findByCode("f1_season").orElseThrow();

    List<OpenF1SessionDto> raceSessions = openF1Client.getRaceSessions(year);
    log.info("F1 레이스 {} 개 조회됨", raceSessions.size());

    for (OpenF1SessionDto session : raceSessions) {
      try {
        List<OpenF1DriverDto> drivers = openF1Client.getDrivers(session.sessionKey());
        transactionTemplate.executeWithoutResult(
            status -> {
              Map<String, Team> teamsByName = syncConstructors(sport, league, drivers);
              Match match = syncMatch(sport, league, session);
              syncMatchTeams(match, teamsByName);
            });
      } catch (Exception e) {
        log.error("레이스 동기화 실패: sessionKey={}, error={}", session.sessionKey(), e.getMessage());
      }
    }

    log.info("F1 시즌 동기화 완료: year={}", year);
  }

  private Map<String, Team> syncConstructors(
      Sport sport, League league, List<OpenF1DriverDto> drivers) {
    return drivers.stream()
        .filter(d -> d.teamName() != null)
        .map(OpenF1DriverDto::teamName)
        .distinct()
        .collect(
            Collectors.toMap(
                teamName -> teamName,
                teamName -> {
                  String externalId = "f1_" + teamName.toLowerCase().replace(" ", "_");
                  return teamRepository
                      .findByExternalId(externalId)
                      .orElseGet(
                          () ->
                              teamRepository.save(
                                  Team.create(
                                      sport,
                                      league,
                                      teamName,
                                      teamName.substring(0, Math.min(teamName.length(), 20)),
                                      externalId)));
                }));
  }

  private Match syncMatch(Sport sport, League league, OpenF1SessionDto session) {
    String externalId = "f1_session_" + session.sessionKey();
    LocalDateTime matchTime = parseDateTime(session.dateStart());
    MatchStatus status = determineStatus(session.dateStart(), session.dateEnd());

    return matchRepository
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
  }

  private void syncMatchTeams(Match match, Map<String, Team> teamsByName) {
    teamsByName
        .values()
        .forEach(
            team -> {
              if (!matchTeamRepository.existsByMatchIdAndTeamId(match.getId(), team.getId())) {
                matchTeamRepository.save(MatchTeam.create(match, team, MatchTeamRole.PARTICIPANT));
              }
            });
  }

  private MatchStatus determineStatus(String dateStartStr, String dateEndStr) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    LocalDateTime dateStart = parseDateTime(dateStartStr);
    LocalDateTime dateEnd = parseDateTime(dateEndStr);

    if (now.isBefore(dateStart)) return MatchStatus.SCHEDULED;
    if (now.isAfter(dateEnd)) return MatchStatus.FINISHED;
    return MatchStatus.LIVE;
  }

  private LocalDateTime parseDateTime(String dateStr) {
    if (dateStr == null) return LocalDateTime.now(ZoneOffset.UTC);
    try {
      return OffsetDateTime.parse(dateStr).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    } catch (Exception e) {
      return LocalDateTime.parse(dateStr);
    }
  }
}
