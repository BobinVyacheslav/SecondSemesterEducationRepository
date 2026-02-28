package com.mipt.todolist.config;

import com.mipt.todolist.repository.StubTaskRepository;
import com.mipt.todolist.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Класс конфигурации для явного определения бинов в контексте Spring
 */
@Configuration
public class Config {
  /**
   * Создает и регистрирует бин репозитория-заглушки
   *
   * @return экземпляр StubTaskRepository
   */
  @Bean
  public TaskRepository stubRepository() {
    return new StubTaskRepository();
  }
}