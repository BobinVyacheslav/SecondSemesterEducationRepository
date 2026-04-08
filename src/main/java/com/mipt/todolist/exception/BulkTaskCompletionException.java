package com.mipt.todolist.exception;

public class BulkTaskCompletionException extends RuntimeException {
  public BulkTaskCompletionException(Long taskId) {
    super("Task with id " + taskId + " not found for bulk completion");
  }
}
