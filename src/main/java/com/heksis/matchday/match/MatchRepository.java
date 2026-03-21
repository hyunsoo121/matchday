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
      ORDER BY m.matchTime
      """)
  List<Match> findAllWithTeams();

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.sport.id = :sportId
      ORDER BY m.matchTime
      """)
  List<Match> findBySportIdWithTeams(@Param("sportId") Long sportId);

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.matchTime >= :from AND m.matchTime < :to
      ORDER BY m.matchTime
      """)
  List<Match> findByDateRangeWithTeams(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.sport.id = :sportId
        AND m.matchTime >= :from AND m.matchTime < :to
      ORDER BY m.matchTime
      """)
  List<Match> findBySportIdAndDateRangeWithTeams(
      @Param("sportId") Long sportId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.status = :status
      ORDER BY m.matchTime
      """)
  List<Match> findByStatusWithTeams(@Param("status") MatchStatus status);

  @Query(
      """
      SELECT DISTINCT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.sport.id = :sportId AND m.status = :status
      ORDER BY m.matchTime
      """)
  List<Match> findBySportIdAndStatusWithTeams(
      @Param("sportId") Long sportId, @Param("status") MatchStatus status);

  @Query(
      """
      SELECT m FROM Match m
      LEFT JOIN FETCH m.teams mt
      LEFT JOIN FETCH mt.team
      WHERE m.id = :id
      """)
  Optional<Match> findByIdWithTeams(@Param("id") Long id);

  Optional<Match> findByExternalId(String externalId);
}
