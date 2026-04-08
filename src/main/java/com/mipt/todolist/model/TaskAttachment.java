package com.mipt.todolist.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Stored task attachment metadata")
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "task_attachments")
public class TaskAttachment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "stored_file_name", nullable = false)
  private String storedFileName;

  @Column(name = "content_type")
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;

  public Long getTaskId() {
    return task == null ? null : task.getId();
  }

  public void setTaskId(Long taskId) {
    if (taskId == null) {
      this.task = null;
      return;
    }

    Task task = new Task();
    task.setId(taskId);
    this.task = task;
  }
}
