package com.heksis.matchday.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  SPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다."),
  LEAGUE_NOT_FOUND(HttpStatus.NOT_FOUND, "리그를 찾을 수 없습니다."),
  TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
  MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "경기를 찾을 수 없습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "즐겨찾기를 찾을 수 없습니다."),
  FAVORITE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 즐겨찾기에 추가된 항목입니다.");

  private final HttpStatus status;
  private final String message;
}
