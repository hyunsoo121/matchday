package com.heksis.matchday.team;

import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.team.dto.TeamResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

  private final TeamService teamService;

  @GetMapping("/search")
  public ApiResponse<List<TeamResponse>> search(
      @RequestParam String q, @RequestParam(required = false) Long sportId) {
    return ApiResponse.ok(teamService.search(q, sportId));
  }

  @GetMapping("/{id}")
  public ApiResponse<TeamResponse> getTeam(@PathVariable Long id) {
    return ApiResponse.ok(teamService.findById(id));
  }
}
