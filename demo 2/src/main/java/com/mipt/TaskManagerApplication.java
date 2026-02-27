package com.mipt;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной класс запуска приложения Task Manager
 * Аннотация @SpringBootApplication включает автоматическую конфигурацию,
 * сканирование компонентов и поиск конфигурационных классов
 */
@SpringBootApplication
public class TaskManagerApplication {

  /**
   * Точка входа в Java-приложение.
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(TaskManagerApplication.class, args);
  }
}
