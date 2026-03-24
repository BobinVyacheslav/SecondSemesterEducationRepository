package com.mipt.todolist.repository;



import com.mipt.todolist.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Основная реализация репозитория, хранящая данные в оперативной памяти
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {
  private final Map<Long, Task> storage = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public List<Task> findAll() { return new ArrayList<>(storage.values()); }

  @Override
  public Optional<Task> findById(Long id) { return Optional.ofNullable(storage.get(id)); }

  @Override
  public Task save(Task task) {
    if (task.getId() == null) {
      task.setId(idGenerator.getAndIncrement());
    }
    storage.put(task.getId(), task);
    return task;
  }

  @Override
  public void deleteById(Long id) { storage.remove(id); }
}
