package com.heksis.matchday.match;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MatchStatusConverter implements AttributeConverter<MatchStatus, String> {

  @Override
  public String convertToDatabaseColumn(MatchStatus attribute) {
    return attribute == null ? null : attribute.name();
  }

  @Override
  public MatchStatus convertToEntityAttribute(String dbData) {
    return dbData == null ? null : MatchStatus.valueOf(dbData);
  }
}
