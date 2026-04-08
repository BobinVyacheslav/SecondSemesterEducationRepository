package com.mipt.todolist.service;
import com.mipt.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

/**
 * Сервис для анализа данных, демонстрирующий работу с аннотациями @Primary и @Qualifier
 */
@Service
public class TaskStatisticsService {
  private final TaskRepository taskRepository;

  /**
   * Конструктор для сервиса, инжектирующего два репозитория
   * @param taskRepository основной JPA-репозиторий
   */
  public TaskStatisticsService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  /**
   * Предоставляет сравнение наполненности двух типов репозиториев
   *
   * @return Строка со статистикой
   */
  public String getComparisonReport() {
    return String.format("Всего задач: %d", taskRepository.findAll().size());
  }
}
