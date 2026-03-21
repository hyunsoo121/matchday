package com.heksis.matchday.sport;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sport")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "name_ko", nullable = false, length = 50)
  private String nameKo;

  @Column(name = "name_en", nullable = false, length = 50)
  private String nameEn;

  @Column(name = "icon_url", length = 255)
  private String iconUrl;

  @Convert(converter = SportCategoryConverter.class)
  @Column(nullable = false, columnDefinition = "sport_category")
  private SportCategory category;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;
}
