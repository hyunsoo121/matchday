package com.heksis.matchday.sport;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {

  List<Sport> findAllByIsActiveTrueOrderByDisplayOrderAsc();

  Optional<Sport> findByCode(String code);
}
