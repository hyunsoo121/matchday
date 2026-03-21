package com.heksis.matchday.sport.dto;

import com.heksis.matchday.sport.Sport;
import com.heksis.matchday.sport.SportCategory;

public record SportResponse(
    Long id,
    String code,
    String nameKo,
    String nameEn,
    String iconUrl,
    SportCategory category,
    int displayOrder) {

  public static SportResponse from(Sport sport) {
    return new SportResponse(
        sport.getId(),
        sport.getCode(),
        sport.getNameKo(),
        sport.getNameEn(),
        sport.getIconUrl(),
        sport.getCategory(),
        sport.getDisplayOrder());
  }
}
