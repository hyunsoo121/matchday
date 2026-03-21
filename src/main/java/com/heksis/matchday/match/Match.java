package com.heksis.matchday.match;

import com.heksis.matchday.league.League;
import com.heksis.matchday.sport.Sport;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "match")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sport_id", nullable = false)
  private Sport sport;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "league_id")
  private League league;

  @Column(name = "match_time", nullable = false)
  private LocalDateTime matchTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "match_status")
  private MatchStatus status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "score_detail", columnDefinition = "jsonb")
  private String scoreDetail;

  @Column(name = "external_id", length = 50)
  private String externalId;

  @Column(name = "external_url", length = 255)
  private String externalUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
  private List<MatchTeam> teams = new ArrayList<>();
}
