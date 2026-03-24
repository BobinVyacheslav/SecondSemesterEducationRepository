package com.mipt.todolist.dto;

import com.mipt.todolist.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Task representation returned to the client")
@Getter
@Setter
@NoArgsConstructor
public class TaskResponseDto {
  @Schema(description = "Task identifier", example = "1")
  private Long id;
  @Schema(description = "Task title")
  private String title;
  @Schema(description = "Task description")
  private String description;
  @Schema(description = "Completion flag")
  private boolean completed;
  @Schema(description = "Creation timestamp")
  private LocalDateTime createdAt;
  @Schema(description = "Due date")
  private LocalDate dueDate;
  @Schema(description = "Task priority")
  private Priority priority;
  @Schema(description = "Task tags")
  private Set<String> tags;
}
