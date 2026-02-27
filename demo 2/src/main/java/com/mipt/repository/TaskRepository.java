package com.mipt.repository;

import com.mipt.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Унифицированный интерфейс для работы с хранилищем задач
 */
public interface TaskRepository {
  List<Task> findAll();

  Optional<Task> findById(String id);

  Task save(Task task);

  void deleteById(String id);
}