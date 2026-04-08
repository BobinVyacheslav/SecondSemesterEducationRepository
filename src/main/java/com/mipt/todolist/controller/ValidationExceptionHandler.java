package com.mipt.todolist.controller;

import com.mipt.todolist.exception.BulkTaskCompletionException;
import com.mipt.todolist.dto.ErrorResponse;
import com.mipt.todolist.exception.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {
  private final Environment environment;

  public ValidationExceptionHandler(Environment environment) {
    this.environment = environment;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return ResponseEntity.badRequest().body(buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Ошибка валидации",
        request.getRequestURI(),
        details));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception,
      HttpServletRequest request) {
    Map<String, Object> details = new LinkedHashMap<>();
    for (var violation : exception.getConstraintViolations()) {
      details.put(violation.getPropertyPath().toString(), violation.getMessage());
    }

    return ResponseEntity.badRequest().body(buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Ошибка ограничения",
        request.getRequestURI(),
        details));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception,
      HttpServletRequest request) {
    return ResponseEntity.badRequest().body(buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        exception.getMessage(),
        request.getRequestURI(),
        Map.of("parameter", exception.getParameterName())));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception,
      HttpServletRequest request) {
    return ResponseEntity.badRequest().body(buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Тело запроса отсутствует или заполнено неверно",
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(
      NoHandlerFoundException exception,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException exception,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTaskNotFound(
      TaskNotFoundException exception,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(BulkTaskCompletionException.class)
  public ResponseEntity<ErrorResponse> handleBulkTaskCompletionException(
      BulkTaskCompletionException exception,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse(
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException exception,
      HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    return ResponseEntity.status(status).body(buildErrorResponse(
        status,
        exception.getReason() == null ? status.getReasonPhrase() : exception.getReason(),
        request.getRequestURI(),
        Map.of()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception exception,
      HttpServletRequest request) {
    boolean productionProfile = isProductionProfileActive();
    String message = productionProfile ? "Внутренняя ошибка сервера" : exception.getMessage();
    Map<String, Object> details = productionProfile ? Map.of() : Map.of("exception", exception.getClass().getSimpleName());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        message,
        request.getRequestURI(),
        details));
  }

  private ErrorResponse buildErrorResponse(
      HttpStatus status,
      String message,
      String path,
      Map<String, Object> details) {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setTimestamp(Instant.now());
    errorResponse.setStatus(status.value());
    errorResponse.setError(status.getReasonPhrase());
    errorResponse.setMessage(message);
    errorResponse.setPath(path);
    errorResponse.setDetails(details);
    return errorResponse;
  }

  private boolean isProductionProfileActive() {
    List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
    return activeProfiles.contains("prod");
  }
}
