package com.mipt.config;

import com.mipt.model.Task;
import com.mipt.repository.StubTaskRepository;
import com.mipt.repository.TaskRepository;
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