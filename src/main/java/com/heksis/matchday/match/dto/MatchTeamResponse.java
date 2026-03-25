package com.heksis.matchday.match.dto;

import com.heksis.matchday.match.MatchTeam;
import com.heksis.matchday.match.MatchTeamResult;
import com.heksis.matchday.match.MatchTeamRole;

public record MatchTeamResponse(
    Long teamId,
    String nameKo,
    String nameEn,
    String logoUrl,
    MatchTeamRole role,
    Integer totalScore,
    MatchTeamResult result) {

  public static MatchTeamResponse from(MatchTeam matchTeam) {
    return new MatchTeamResponse(
        matchTeam.getTeam().getId(),
        matchTeam.getTeam().getNameKo(),
        matchTeam.getTeam().getNameEn(),
        matchTeam.getTeam().getLogoUrl(),
        matchTeam.getRole(),
        matchTeam.getTotalScore(),
        matchTeam.getResult());
  }
}
