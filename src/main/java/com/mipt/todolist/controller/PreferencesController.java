package com.mipt.todolist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "Preferences stored in cookies")
public class PreferencesController {
  private static final String COOKIE_NAME = "viewPreference";
  private static final String DEFAULT_MODE = "compact";

  @GetMapping("/view")
  @Operation(summary = "Get current view preference")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "View preference returned")
  })
  public ResponseEntity<Map<String, String>> getViewPreference(
      @CookieValue(name = COOKIE_NAME, required = false) String viewPreference) {
    String mode = viewPreference == null ? DEFAULT_MODE : viewPreference;
    return ResponseEntity.ok()
        .header("Set-Cookie", buildCookie(mode).toString())
        .body(Map.of("mode", mode));
  }

  @PostMapping("/view")
  @Operation(summary = "Update view preference")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "View preference updated"),
      @ApiResponse(responseCode = "400", description = "Invalid mode")
  })
  public ResponseEntity<Map<String, String>> updateViewPreference(@RequestParam String mode) {
    if (!"compact".equals(mode) && !"detailed".equals(mode)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр mode должен быть compact или detailed");
    }

    return ResponseEntity.ok()
        .header("Set-Cookie", buildCookie(mode).toString())
        .body(Map.of("mode", mode));
  }

  private ResponseCookie buildCookie(String mode) {
    return ResponseCookie.from(COOKIE_NAME, mode)
        .httpOnly(false)
        .path("/")
        .sameSite("Lax")
        .build();
  }
}
