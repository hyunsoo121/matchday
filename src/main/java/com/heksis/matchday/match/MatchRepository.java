package com.heksis.matchday.match;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE (:sportId IS NULL OR m.sport.id = :sportId)
        AND (:leagueId IS NULL OR m.league.id = :leagueId)
        AND (:status IS NULL OR m.status = :status)
        AND (:from IS NULL OR m.matchTime >= :from)
        AND (:to IS NULL OR m.matchTime < :to)
      ORDER BY m.matchTime
      """)
  List<Match> findWithFilters(
      @Param("sportId") Long sportId,
      @Param("leagueId") Long leagueId,
      @Param("status") MatchStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query(
      """
      SELECT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.id = :id
      """)
  Optional<Match> findByIdWithTeams(@Param("id") Long id);
}
