package com.heksis.matchday.league;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Long> {

  List<League> findAllBySportIdAndIsActiveTrueOrderByDisplayOrderAsc(Long sportId);
}
