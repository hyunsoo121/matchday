package com.heksis.matchday.sport;

public enum SportCategory {
  SPORTS,
  ESPORTS,
  MOTORSPORTS;

  public String toDbValue() {
    return name().toLowerCase();
  }
}
