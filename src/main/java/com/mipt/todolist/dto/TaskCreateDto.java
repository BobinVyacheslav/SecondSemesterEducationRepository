package com.mipt.todolist.dto;

import com.mipt.todolist.model.Priority;
import com.mipt.todolist.validation.groups.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;


@Schema(description = "Payload for task creation")
@Getter
@Setter
@NoArgsConstructor
public class TaskCreateDto {
  @Schema(description = "Task title", example = "Finish homework")
  @NotBlank(groups = OnCreate.class)
  @Size(min = 3, max = 100, groups = OnCreate.class)
  private String title;
  @Schema(description = "Task description", example = "Complete remaining parts of the assignment")
  @Size(max = 500, groups = OnCreate.class)
  private String description;
  @Schema(description = "Due date", example = "2026-03-30")
  @FutureOrPresent(groups = OnCreate.class)
  private LocalDate dueDate;
  @Schema(description = "Task priority", example = "HIGH")
  @NotNull(groups = OnCreate.class)
  private Priority priority;
  @Schema(description = "Task tags")
  @Size(max = 5, groups = OnCreate.class)
  private Set<String> tags;
}
