package com.heksis.matchday.sport;

import com.heksis.matchday.global.dto.ApiResponse;
import com.heksis.matchday.sport.dto.SportResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sports")
@RequiredArgsConstructor
public class SportController {

  private final SportService sportService;

  @GetMapping
  public ApiResponse<List<SportResponse>> getSports() {
    return ApiResponse.ok(sportService.findAllActive());
  }

  @GetMapping("/{id}")
  public ApiResponse<SportResponse> getSport(@PathVariable Long id) {
    return ApiResponse.ok(sportService.findById(id));
  }
}
