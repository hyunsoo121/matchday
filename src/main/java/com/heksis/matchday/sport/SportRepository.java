package com.heksis.matchday.sport;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {

  List<Sport> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}
