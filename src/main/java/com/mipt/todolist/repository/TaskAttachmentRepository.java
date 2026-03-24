package com.mipt.todolist.repository;

import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TaskAttachmentRepository {
  private final Map<Long, TaskAttachment> storage = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  public List<TaskAttachment> findAll() {
    return new ArrayList<>(storage.values());
  }

  public Optional<TaskAttachment> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  public TaskAttachment save(TaskAttachment taskAttachment) {
    if (taskAttachment.getId() == 0) {
      taskAttachment.setId(idGenerator.getAndIncrement());
    }
    storage.put(taskAttachment.getId(), taskAttachment);
    return taskAttachment;
  }

  public void deleteById(Long id) {
    storage.remove(id);
  }

  public List<TaskAttachment> findByTaskId(long id) {
    ArrayList<TaskAttachment> list = new ArrayList<>();
    for (Map.Entry<Long, TaskAttachment> entry : storage.entrySet()) {
      if (Objects.equals(entry.getValue().getTaskId(), id)) {
        list.add(entry.getValue());
      }
    }
    return list;
  }
}
