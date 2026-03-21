package com.heksis.matchday.match;

import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.match.dto.MatchResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

  private final MatchService matchService;

  @GetMapping
  public ApiResponse<List<MatchResponse>> getMatches(
      @RequestParam(required = false) Long sportId,
      @RequestParam(required = false) Long leagueId,
      @RequestParam(required = false) MatchStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {
    return ApiResponse.ok(matchService.findMatches(sportId, leagueId, status, date));
  }

  @GetMapping("/today")
  public ApiResponse<List<MatchResponse>> getToday(@RequestParam(required = false) Long sportId) {
    return ApiResponse.ok(matchService.findToday(sportId));
  }

  @GetMapping("/live")
  public ApiResponse<List<MatchResponse>> getLive(@RequestParam(required = false) Long sportId) {
    return ApiResponse.ok(matchService.findLive(sportId));
  }

  @GetMapping("/{id}")
  public ApiResponse<MatchResponse> getMatch(@PathVariable Long id) {
    return ApiResponse.ok(matchService.findById(id));
  }
}
