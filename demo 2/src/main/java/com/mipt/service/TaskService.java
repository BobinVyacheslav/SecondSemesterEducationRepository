package com.mipt.service;

import com.mipt.model.Task;
import com.mipt.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  /**
   * Возвращает список всех существующих задач.
   * * @return список задач из репозитория
   */

  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  /**
   * Выполняет поиск задачи по её уникальному идентификатору.
   * * @param id идентификатор задачи
   *
   * @return Optional, содержащий задачу, если она найдена
   */
  public Optional<Task> getTaskById(String id) {
    return taskRepository.findById(id);
  }

  /**
   * Сохраняет новую задачу или обновляет существующую в репозитории
   * * @param task объект задачи для сохранения
   *
   * @return сохраненный объект задачи
   */
  public Task saveTask(Task task) {
    return taskRepository.save(task);
  }

  /**
   * Удаляет задачу из системы по её идентификатору
   * * @param id идентификатор задачи для удаления
   */
  public void deleteTask(String id) {
    taskRepository.deleteById(id);
  }
}
