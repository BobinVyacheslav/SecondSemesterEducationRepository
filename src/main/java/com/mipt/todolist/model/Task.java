package com.mipt.todolist.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Основной класс модели, представляющий задачу в системе
 */
@Getter
@Setter
@NoArgsConstructor
public class Task {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
  private LocalDateTime createdAt;
  private LocalDate dueDate;
  private Priority priority;
  private Set<String> tags;

  public Task(Long id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.createdAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return completed == task.completed
        && Objects.equals(id, task.id)
        && Objects.equals(title, task.title)
        && Objects.equals(description, task.description)
        && Objects.equals(createdAt, task.createdAt)
        && Objects.equals(dueDate, task.dueDate)
        && priority == task.priority
        && Objects.equals(tags, task.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed, createdAt, dueDate, priority, tags);
  }

  @Override
  public String toString() {
    return String.format(
        "Task[id='%s', title='%s', completed=%b, createdAt=%s, dueDate=%s, priority=%s, tags=%s]",
        id,
        title,
        completed,
        createdAt,
        dueDate,
        priority,
        tags);
  }
}
