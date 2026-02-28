package com.mipt.todolist.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Бин с областью видимости prototype.
 * Создает новый экземпляр при каждом обращении к контейнеру.
 */
@Component
@Scope("prototype")
public class PrototypeScopedBean {
  /**
   * Генерирует уникальный идентификатор на базе UUID.
   * @return строковое представление UUID.
   */
  public String generateId() {
    return UUID.randomUUID().toString();
  }
}