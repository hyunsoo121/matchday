package com.heksis.matchday.match;

import com.heksis.matchday.team.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchTeam {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "match_id", nullable = false)
  private Match match;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MatchTeamRole role;

  @Column(name = "total_score")
  private Integer totalScore;

  public static MatchTeam create(Match match, Team team, MatchTeamRole role) {
    MatchTeam matchTeam = new MatchTeam();
    matchTeam.match = match;
    matchTeam.team = team;
    matchTeam.role = role;
    return matchTeam;
  }
}
