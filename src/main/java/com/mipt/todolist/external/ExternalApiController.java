package com.mipt.todolist.external;

import com.mipt.todolist.dto.ExternalTaskCreateRequest;
import com.mipt.todolist.dto.ExternalTaskResponse;
import com.mipt.todolist.dto.ExternalTaskUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {
  private final AtomicLong idGenerator = new AtomicLong(0);
  private final Map<Long, ExternalTaskResponse> tasks = new ConcurrentHashMap<>();

  @PostMapping("/tasks")
  public ResponseEntity<ExternalTaskResponse> createTask(
      @Valid @RequestBody ExternalTaskCreateRequest request) {
    long id = idGenerator.incrementAndGet();
    ExternalTaskResponse response = new ExternalTaskResponse(id, request.title(), request.description(), false);
    tasks.put(id, response);
    URI location = URI.create("/external/v1/tasks/" + id);
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/tasks/{id}")
  public ResponseEntity<?> getTask(@PathVariable Long id) {
    ExternalTaskResponse response = tasks.get(id);
    if (response == null) {
      return notFound(id);
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping("/tasks")
  public List<ExternalTaskResponse> getTasks(
      @RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) Integer limit) {
    return tasks.values().stream()
        .filter(task -> completed == null || task.completed() == completed)
        .sorted(Comparator.comparing(ExternalTaskResponse::id))
        .limit(limit == null ? Long.MAX_VALUE : limit)
        .toList();
  }

  @PutMapping("/tasks/{id}")
  public ResponseEntity<?> updateTask(
      @PathVariable Long id,
      @Valid @RequestBody ExternalTaskUpdateRequest request) {
    if (!tasks.containsKey(id)) {
      return notFound(id);
    }

    ExternalTaskResponse response = new ExternalTaskResponse(
        id,
        request.title(),
        request.description(),
        request.completed());
    tasks.put(id, response);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<?> deleteTask(@PathVariable Long id) {
    if (tasks.remove(id) == null) {
      return notFound(id);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unstable")
  public ResponseEntity<?> unstable(@RequestParam String mode) throws InterruptedException {
    return switch (mode) {
      case "timeout" -> {
        Thread.sleep(Duration.ofSeconds(5).toMillis());
        yield ResponseEntity.ok(Map.of("status", "late-response"));
      }
      case "500" -> {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "External API failed");
        problemDetail.setTitle("Internal Server Error");
        yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
      }
      case "429" -> {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded");
        problemDetail.setTitle("Too Many Requests");
        yield ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header(HttpHeaders.RETRY_AFTER, "5")
            .body(problemDetail);
      }
      case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .contentType(MediaType.TEXT_HTML)
          .body("<html><body><h1>Bad Gateway</h1></body></html>");
      default -> ResponseEntity.badRequest().body(Map.of(
          "message", "Unsupported mode",
          "supportedModes", List.of("timeout", "500", "429", "html")));
    };
  }

  private ResponseEntity<ProblemDetail> notFound(Long id) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.NOT_FOUND,
        "Task " + id + " not found");
    problemDetail.setTitle("Not Found");
    problemDetail.setProperty("taskId", id);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
  }
}
