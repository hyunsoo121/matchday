package com.heksis.matchday.match;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MatchTeamRoleConverter implements AttributeConverter<MatchTeamRole, String> {

  @Override
  public String convertToDatabaseColumn(MatchTeamRole attribute) {
    return attribute == null ? null : attribute.name();
  }

  @Override
  public MatchTeamRole convertToEntityAttribute(String dbData) {
    return dbData == null ? null : MatchTeamRole.valueOf(dbData);
  }
}
