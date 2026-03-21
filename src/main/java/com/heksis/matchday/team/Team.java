package com.heksis.matchday.team;

import com.heksis.matchday.league.League;
import com.heksis.matchday.sport.Sport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sport_id", nullable = false)
  private Sport sport;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "league_id")
  private League league;

  @Column(name = "name_ko", nullable = false, length = 100)
  private String nameKo;

  @Column(name = "name_en", nullable = false, length = 100)
  private String nameEn;

  @Column(name = "short_name", length = 20)
  private String shortName;

  @Column(name = "logo_url", length = 255)
  private String logoUrl;

  @Column(name = "external_id", length = 50)
  private String externalId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
