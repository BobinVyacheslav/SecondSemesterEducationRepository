package com.mipt.todolist.exception;

public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(Long taskId) {
    super("Task with id " + taskId + " not found");
  }

  public TaskNotFoundException(String message) {
    super(message);
  }
}
