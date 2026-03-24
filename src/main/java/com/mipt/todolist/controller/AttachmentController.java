package com.mipt.todolist.controller;

import com.mipt.todolist.dto.AttachmentResponseDto;
import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.service.AttachmentService;
import com.mipt.todolist.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Attachments", description = "Task attachment management")
public class AttachmentController {
  private final AttachmentService attachmentService;
  private final TaskService taskService;

  public AttachmentController(AttachmentService attachmentService, TaskService taskService) {
    this.attachmentService = attachmentService;
    this.taskService = taskService;
  }

  @PostMapping("/tasks/{taskId}/attachments")
  @Operation(summary = "Upload attachment to a task")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Attachment uploaded"),
      @ApiResponse(responseCode = "404", description = "Task not found", content = @Content(schema = @Schema(implementation = com.mipt.todolist.dto.ErrorResponse.class)))
  })
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
      @PathVariable Long taskId,
      @RequestParam("file") MultipartFile file) {
    taskService.getRequiredTask(taskId);
    TaskAttachment attachment = attachmentService.storeAttachment(taskId, file);
    AttachmentResponseDto dto = toResponseDto(attachment);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @GetMapping("/attachments/{attachmentId}")
  @Operation(summary = "Download attachment")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Attachment downloaded"),
      @ApiResponse(responseCode = "404", description = "Attachment not found")
  })
  public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
    TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
    Resource resource = attachmentService.loadAsResource(attachmentId);
    MediaType mediaType = attachment.getContentType() == null
        ? MediaType.APPLICATION_OCTET_STREAM
        : MediaType.parseMediaType(attachment.getContentType());

    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename(attachment.getFileName())
            .build()
            .toString())
        .body(resource);
  }

  @DeleteMapping("/attachments/{attachmentId}")
  @Operation(summary = "Delete attachment")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Attachment deleted"),
      @ApiResponse(responseCode = "404", description = "Attachment not found")
  })
  public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
    attachmentService.deleteAttachment(attachmentId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/tasks/{taskId}/attachments")
  @Operation(summary = "List task attachments")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Attachment metadata list returned"),
      @ApiResponse(responseCode = "404", description = "Task not found")
  })
  public ResponseEntity<List<AttachmentResponseDto>> getTaskAttachments(@PathVariable Long taskId) {
    taskService.getRequiredTask(taskId);
    List<TaskAttachment> attachmentList = attachmentService.getAttachmentsByTaskId(taskId);
    List<AttachmentResponseDto> attachments = new java.util.ArrayList<>();
    for (TaskAttachment attachment : attachmentList) {
      attachments.add(toResponseDto(attachment));
    }
    return ResponseEntity.ok(attachments);
  }

  private AttachmentResponseDto toResponseDto(TaskAttachment attachment) {
    AttachmentResponseDto dto = new AttachmentResponseDto();
    dto.setId(attachment.getId());
    dto.setFileName(attachment.getFileName());
    dto.setSize(attachment.getSize());
    dto.setUploadedAt(attachment.getUploadedAt());
    return dto;
  }
}
