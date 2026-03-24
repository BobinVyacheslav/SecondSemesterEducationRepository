package com.mipt.todolist.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Stored task attachment metadata")
@Getter
@Setter
@NoArgsConstructor
public class TaskAttachment {
  private long id;
  private long taskId;
  private String fileName;
  private String storedFileName;
  private String contentType;
  private long size;
  private LocalDateTime uploadedAt;
}
