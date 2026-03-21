package com.heksis.matchday.team;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

  Optional<Team> findByExternalId(String externalId);

  @Query(
      """
      SELECT t FROM Team t
      WHERE (:sportId IS NULL OR t.sport.id = :sportId)
        AND (LOWER(t.nameKo) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(t.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY t.nameKo
      """)
  List<Team> search(@Param("q") String q, @Param("sportId") Long sportId);
}
