package com.mipt.todolist.controller;

import com.mipt.todolist.dto.TaskCreateDto;
import com.mipt.todolist.dto.TaskResponseDto;
import com.mipt.todolist.dto.TaskUpdateDto;
import com.mipt.todolist.mapper.TaskMapper;
import com.mipt.todolist.model.RequestScopedBean;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.service.TaskService;
import com.mipt.todolist.validation.groups.OnCreate;
import com.mipt.todolist.validation.groups.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Основной REST-контроллер для управления задачами.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

  private final TaskService taskService;
  private final RequestScopedBean requestScopedBean;
  private final TaskMapper taskMapper;

  /**
   * Конструктор с внедрением сервиса и бина области запроса.
   *
   * @param taskService       сервис задач.
   * @param requestScopedBean бин текущего запроса.
   */
  public TaskController(TaskService taskService, RequestScopedBean requestScopedBean, TaskMapper taskMapper) {
    this.taskService = taskService;
    this.requestScopedBean = requestScopedBean;
    this.taskMapper = taskMapper;
  }

  /**
   * Получает список всех задач.
   *
   * @return список задач и HTTP 200.
   */
  @GetMapping
  @Operation(summary = "Get all tasks")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Task list returned")
  })
  public ResponseEntity<List<TaskResponseDto>> getAll() {
    System.out.println("Обработка запроса: " + requestScopedBean.getRequestId());
    List<Task> tasks = taskService.getAllTasks();
    List<TaskResponseDto> dtoList = new java.util.ArrayList<>();
    for (Task task : tasks) {
      dtoList.add(taskMapper.toResponseDto(task));
    }

    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(dtoList.size()))
        .body(dtoList);
  }

  /**
   * Возвращает задачу по идентификатору.
   *
   * @param id уникальный ключ задачи.
   * @return задача или 404 если не найдена.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get task by id")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Task returned"),
      @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> getById(@PathVariable Long id) {
    if (taskService.getTaskById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Task task = taskService.getTaskById(id).get();
    TaskResponseDto dto = taskMapper.toResponseDto(task);
    return ResponseEntity.ok(dto);
  }

  /**
   * Создает новую задачу.
   *
   * @param dto данные задачи.
   * @return созданная задача с ID.
   */
  @PostMapping
  @Operation(summary = "Create task")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Task created"),
      @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> create(@RequestBody @Validated(OnCreate.class) TaskCreateDto dto) {
    Task task = taskMapper.toEntity(dto);
    task.setCompleted(false);
    Task saved = taskService.saveTask(task);
    TaskResponseDto responseDto = taskMapper.toResponseDto(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  /**
   * Обновляет существующую задачу.
   *
   * @param id  идентификатор задачи.
   * @param dto новые данные.
   * @return обновленная задача или 404.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update task")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Task updated"),
      @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class)))
  })
  public ResponseEntity<TaskResponseDto> update(
      @PathVariable Long id,
      @RequestBody @Validated(OnUpdate.class) TaskUpdateDto dto) {
    if (taskService.getTaskById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Task existingTask = taskService.getTaskById(id).get();
    Task updatedTask = taskMapper.updateEntity(dto, existingTask);
    Task savedTask = taskService.saveTask(updatedTask);
    TaskResponseDto responseDto = taskMapper.toResponseDto(savedTask);
    return ResponseEntity.ok(responseDto);
  }

  /**
   * Удаляет задачу из системы.
   *
   * @param id идентификатор задачи.
   * @return 204 No Content или 404.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete task")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Task deleted"),
      @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class)))
  })
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (taskService.getTaskById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }
}
