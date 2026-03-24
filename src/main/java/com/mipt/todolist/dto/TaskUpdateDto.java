package com.mipt.todolist.dto;

import com.mipt.todolist.model.Priority;
import com.mipt.todolist.validation.annotation.DueDateNotBeforeCreation;
import com.mipt.todolist.validation.groups.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@DueDateNotBeforeCreation(groups = OnUpdate.class)
@Schema(description = "Payload for partial task update")
@Getter
@Setter
@NoArgsConstructor
public class TaskUpdateDto {
  @Schema(description = "Task title", example = "Finish homework")
  @Size(min = 3, max = 100, groups = OnUpdate.class)
  private String title;
  @Schema(description = "Task description")
  @Size(max = 500, groups = OnUpdate.class)
  private String description;
  @Schema(description = "Task completion flag", example = "true")
  private Boolean completed;
  @Schema(description = "Due date", example = "2026-03-30")
  @FutureOrPresent(groups = OnUpdate.class)
  private LocalDate dueDate;
  @Schema(description = "Task priority", example = "MEDIUM")
  private Priority priority;
  @Schema(description = "Task tags")
  @Size(max = 5, groups = OnUpdate.class)
  private Set<String> tags;
}
