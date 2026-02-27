package com.mipt.repository;



import com.mipt.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Основная реализация репозитория, хранящая данные в оперативной памяти
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {
  private final Map<String, Task> storage = new ConcurrentHashMap<>();

  @Override
  public List<Task> findAll() { return new ArrayList<>(storage.values()); }

  @Override
  public Optional<Task> findById(String id) { return Optional.ofNullable(storage.get(id)); }

  @Override
  public Task save(Task task) {
    storage.put(task.getId(), task);
    return task;
  }

  @Override
  public void deleteById(String id) { storage.remove(id); }
}