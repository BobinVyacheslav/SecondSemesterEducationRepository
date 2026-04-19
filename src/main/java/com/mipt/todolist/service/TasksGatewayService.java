package com.mipt.todolist.service;

import com.mipt.todolist.client.ExternalTasksClient;
import com.mipt.todolist.dto.ExternalTaskCreateRequest;
import com.mipt.todolist.dto.ExternalTaskCreatedResult;
import com.mipt.todolist.dto.ExternalTaskResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TasksGatewayService {
  private static final Logger log = LoggerFactory.getLogger(TasksGatewayService.class);

  private final ExternalTasksClient externalTasksClient;

  public TasksGatewayService(ExternalTasksClient externalTasksClient) {
    this.externalTasksClient = externalTasksClient;
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskFallback")
  public ExternalTaskCreatedResult createTask(ExternalTaskCreateRequest request) {
    return externalTasksClient.createTask(request);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskFallback")
  public ExternalTaskResponse getTask(Long id) {
    return externalTasksClient.getTask(id);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "getTasksFallback")
  public List<ExternalTaskResponse> getTasks(Boolean completed, Integer limit) {
    return externalTasksClient.getTasks(completed, limit);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskFallback")
  public boolean deleteTask(Long id) {
    externalTasksClient.deleteTask(id);
    return true;
  }

  private ExternalTaskCreatedResult createTaskFallback(ExternalTaskCreateRequest request, Throwable throwable) {
    log.warn("Fallback for createTask: {}", throwable.getMessage());
    ExternalTaskResponse fallbackTask = new ExternalTaskResponse(
        null,
        request.title(),
        "Task was not created in the external API. Fallback response.",
        false);
    return new ExternalTaskCreatedResult(fallbackTask, null);
  }

  private ExternalTaskResponse getTaskFallback(Long id, Throwable throwable) {
    log.warn("Fallback for getTask {}: {}", id, throwable.getMessage());
    return new ExternalTaskResponse(
        id,
        "Fallback task",
        "External API is temporarily unavailable",
        false);
  }

  private List<ExternalTaskResponse> getTasksFallback(Boolean completed, Integer limit, Throwable throwable) {
    log.warn("Fallback for getTasks: {}", throwable.getMessage());
    return List.of();
  }

  private boolean deleteTaskFallback(Long id, Throwable throwable) {
    log.warn("Fallback for deleteTask {}: {}", id, throwable.getMessage());
    return false;
  }
}
