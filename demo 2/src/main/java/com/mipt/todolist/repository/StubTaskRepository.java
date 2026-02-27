package com.mipt.todolist.repository;

import com.mipt.todolist.model.Task;

import java.util.*;

/**
 * Репозиторий-заглушка, используемый для тестирования или демонстрации фиксированных данных.
 */
public class StubTaskRepository implements TaskRepository {
  @Override
  public List<Task> findAll() {
    return List.of(new Task("id", "matanaliz", "tyulenev", false));
  }

  @Override
  public Optional<Task> findById(String id) {
    return findAll().stream().filter(t -> t.getId().equals(id)).findFirst();
  }

  @Override
  public Task save(Task task) {
    return task;
  }

  @Override
  public void deleteById(String id) {
  }
}