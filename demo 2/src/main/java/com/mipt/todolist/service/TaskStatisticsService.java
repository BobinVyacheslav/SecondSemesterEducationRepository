package com.mipt.todolist.service;


import com.mipt.todolist.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Сервис для анализа данных, демонстрирующий работу с аннотациями @Primary и @Qualifier
 */
@Service
public class TaskStatisticsService {
  private final TaskRepository primaryRepo;
  private final TaskRepository stubRepo;

  /**
   * Конструктор для сервиса, инжектирующего два репозитория
   * @param primaryRepo InMemoryTaskRepository, так как есть аннотация @Primary
   * @param stubRepo StubTaskRepository, так как есть @Qualifier
   */

  public TaskStatisticsService(TaskRepository primaryRepo,
                               @Qualifier("stubRepository") TaskRepository stubRepo) {
    this.primaryRepo = primaryRepo;
    this.stubRepo = stubRepo;
  }

  /**
   * Предоставляет сравнение наполненности двух типов репозиториев
   *
   * @return Строка со статистикой
   */
  public String getComparisonReport() {
    return String.format("Основной: %d, Заглушка: %d",
        primaryRepo.findAll().size(), stubRepo.findAll().size());
  }
}
