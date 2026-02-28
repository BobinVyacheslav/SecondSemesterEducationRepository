package com.mipt.todolist;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Основной класс запуска приложения Task Manager
 * Аннотация @SpringBootApplication включает автоматическую конфигурацию,
 * сканирование компонентов и поиск конфигурационных классов
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class TaskManagerApplication {

  /**
   * Точка входа в Java-приложение.
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(TaskManagerApplication.class, args);
  }
}
