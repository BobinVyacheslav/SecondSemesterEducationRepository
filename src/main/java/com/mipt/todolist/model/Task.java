package com.mipt.todolist.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Основной класс модели, представляющий задачу в системе
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private boolean completed;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Priority priority;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
  @Column(name = "tag", nullable = false, length = 100)
  private Set<String> tags = new LinkedHashSet<>();

  @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.REMOVE, orphanRemoval = true)
  private Set<TaskAttachment> attachments = new LinkedHashSet<>();

  public Task(Long id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
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
