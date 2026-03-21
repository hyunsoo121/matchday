package com.heksis.matchday.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  SPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다."),
  LEAGUE_NOT_FOUND(HttpStatus.NOT_FOUND, "리그를 찾을 수 없습니다."),
  TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;
}
