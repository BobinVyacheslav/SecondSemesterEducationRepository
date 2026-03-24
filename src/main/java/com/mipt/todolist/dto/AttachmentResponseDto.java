package com.mipt.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Attachment metadata returned to the client")
@Getter
@Setter
@NoArgsConstructor
public class AttachmentResponseDto {
  @Schema(description = "Attachment identifier", example = "1")
  private Long id;

  @Schema(description = "Original file name", example = "notes.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "1024")
  private long size;

  @Schema(description = "Upload timestamp")
  private LocalDateTime uploadedAt;
}
