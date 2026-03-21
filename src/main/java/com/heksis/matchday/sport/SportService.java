package com.heksis.matchday.sport;

import com.heksis.matchday.global.exception.BusinessException;
import com.heksis.matchday.global.exception.ErrorCode;
import com.heksis.matchday.sport.dto.SportResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SportService {

  private final SportRepository sportRepository;

  public List<SportResponse> findAllActive() {
    return sportRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc().stream()
        .map(SportResponse::from)
        .toList();
  }

  public SportResponse findById(Long id) {
    Sport sport =
        sportRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SPORT_NOT_FOUND));
    return SportResponse.from(sport);
  }
}
