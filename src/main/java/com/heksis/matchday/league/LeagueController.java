package com.heksis.matchday.league;

import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.league.dto.LeagueResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LeagueController {

  private final LeagueService leagueService;

  @GetMapping("/sports/{sportId}/leagues")
  public ApiResponse<List<LeagueResponse>> getLeaguesBySport(@PathVariable Long sportId) {
    return ApiResponse.ok(leagueService.findBySport(sportId));
  }

  @GetMapping("/leagues/{id}")
  public ApiResponse<LeagueResponse> getLeague(@PathVariable Long id) {
    return ApiResponse.ok(leagueService.findById(id));
  }
}