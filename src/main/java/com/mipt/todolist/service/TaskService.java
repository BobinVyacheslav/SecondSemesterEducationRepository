package com.mipt.todolist.service;

import com.mipt.todolist.model.Task;
import com.mipt.todolist.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления задачами, дополненный механизмом кэширования
 * и обработкой событий жизненного цикла бина
 */
@Service
public class TaskService {
  private final TaskRepository taskRepository;
  private final Map<String, Task> taskCache = new ConcurrentHashMap<>();

  /**
   * Создает экземпляр сервиса с внедрением репозитория
   * @param taskRepository основной репозиторий для работы с данными.
   */
  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  /**
   * Инициализирует кэш задач сразу после создания бина и внедрения зависимостей
   * Загружает все существующие задачи из репозитория в локальную карту
   */
  @PostConstruct
  public void initCache() {
    taskRepository.findAll().forEach(task -> taskCache.put(task.getId(), task));
    System.out.println("Кэш TaskService инициализирован. Загружено задач: " + taskCache.size());
  }

  /**
   * Выполняет очистку ресурсов перед уничтожением бина Spring-контейнером
   * Выводит статистику использования кэша в консоль.
   */
  @PreDestroy
  public void cleanup() {
    System.out.println("Завершение работы TaskService. Количество задач в кэше перед очисткой: " + taskCache.size());
    taskCache.clear();
  }

  /**
   * Возвращает список всех задач. В данной реализации данные берутся напрямую из репозитория
   * @return список всех задач
   */
  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  /**
   * Ищет задачу по её уникальному идентификатору
   * @param id идентификатор задачи.
   * @return Optional с найденной задачей или пустой, если задача не найдена
   */
  public Optional<Task> getTaskById(String id) {
    return taskRepository.findById(id);
  }

  /**
   * Сохраняет задачу в репозиторий и одновременно обновляет запись в кэше
   * @param task объект задачи для сохранения
   * @return сохраненный объект задачи.
   */
  public Task saveTask(Task task) {
    Task savedTask = taskRepository.save(task);
    taskCache.put(savedTask.getId(), savedTask);
    return savedTask;
  }

  /**
   * Удаляет задачу по идентификатору из репозитория и кэша
   * @param id идентификатор задачи для удаления
   */
  public void deleteTask(String id) {
    taskRepository.deleteById(id);
    taskCache.remove(id);
  }
}