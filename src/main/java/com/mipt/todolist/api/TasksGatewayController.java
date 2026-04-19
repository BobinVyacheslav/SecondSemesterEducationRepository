package com.mipt.todolist.api;

import com.mipt.todolist.dto.ExternalTaskCreateRequest;
import com.mipt.todolist.dto.ExternalTaskCreatedResult;
import com.mipt.todolist.dto.ExternalTaskResponse;
import com.mipt.todolist.service.TasksGatewayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks")
public class TasksGatewayController {
  private final TasksGatewayService tasksGatewayService;

  public TasksGatewayController(TasksGatewayService tasksGatewayService) {
    this.tasksGatewayService = tasksGatewayService;
  }

  @PostMapping
  public ResponseEntity<ExternalTaskResponse> createTask(@Valid @RequestBody ExternalTaskCreateRequest request) {
    ExternalTaskCreatedResult result = tasksGatewayService.createTask(request);
    URI location = result.location();

    if (location != null) {
      return ResponseEntity.created(location).body(result.task());
    }

    return ResponseEntity.ok(result.task());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExternalTaskResponse> getTask(@PathVariable Long id) {
    return ResponseEntity.ok(tasksGatewayService.getTask(id));
  }

  @GetMapping
  public ResponseEntity<List<ExternalTaskResponse>> getTasks(
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(tasksGatewayService.getTasks(completed, limit));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteTask(@PathVariable Long id) {
    boolean deleted = tasksGatewayService.deleteTask(id);
    if (deleted) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.status(503).body(Map.of(
        "message", "Delete operation is temporarily unavailable",
        "taskId", id));
  }
}
