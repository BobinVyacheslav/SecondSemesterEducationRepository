package com.mipt.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Unified API error response")
@Getter
@Setter
@NoArgsConstructor
public class ErrorResponse {
  @Schema(description = "Error timestamp")
  private Instant timestamp;
  @Schema(description = "HTTP status code", example = "400")
  private int status;
  @Schema(description = "HTTP reason phrase", example = "Bad Request")
  private String error;
  @Schema(description = "Client-facing message")
  private String message;
  @Schema(description = "Request path", example = "/api/tasks")
  private String path;
  @Schema(description = "Additional error details")
  private Map<String, Object> details;
}
