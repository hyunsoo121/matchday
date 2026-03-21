package com.heksis.matchday.sport;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SportCategoryConverter implements AttributeConverter<SportCategory, String> {

  @Override
  public String convertToDatabaseColumn(SportCategory attribute) {
    return attribute == null ? null : attribute.toDbValue();
  }

  @Override
  public SportCategory convertToEntityAttribute(String dbData) {
    return dbData == null ? null : SportCategory.valueOf(dbData.toUpperCase());
  }
}
