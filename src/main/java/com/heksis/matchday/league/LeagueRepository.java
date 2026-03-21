package com.heksis.matchday.league;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Long> {

  List<League> findAllBySportIdAndIsActiveTrueOrderByDisplayOrderAsc(Long sportId);

  Optional<League> findByCode(String code);
}
