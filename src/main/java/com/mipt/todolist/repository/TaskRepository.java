package com.mipt.todolist.repository;

import com.mipt.todolist.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Унифицированный интерфейс для работы с хранилищем задач
 */
public interface TaskRepository {
  List<Task> findAll();

  Optional<Task> findById(Long id);

  Task save(Task task);

  void deleteById(Long id);
}
