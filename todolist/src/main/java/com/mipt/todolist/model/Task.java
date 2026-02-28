package com.mipt.todolist.model;

import java.util.Objects;

/**
 * Основной класс модели, представляющий задачу в системе
 */
public class Task {
  private String id;
  private String title;
  private String description;
  private boolean completed;

  public Task() {
  }

  public Task(String id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return completed == task.completed && Objects.equals(id, task.id) &&
        Objects.equals(title, task.title) && Objects.equals(description, task.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed);
  }

  @Override
  public String toString() {
    return String.format("Task[id='%s', title='%s', completed=%b]", id, title, completed);
  }
}