package com.mipt.todolist.service;

import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.repository.TaskRepository;
import com.mipt.todolist.repository.TaskAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
  private final TaskAttachmentRepository attachmentRepository;
  private final TaskRepository taskRepository;
  private final Path uploadDirectory;

  public AttachmentService(
      TaskAttachmentRepository attachmentRepository,
      TaskRepository taskRepository,
      @Value("${app.attachments.upload-dir}") String uploadDirectory) {
    this.attachmentRepository = attachmentRepository;
    this.taskRepository = taskRepository;
    this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
  }

  public TaskAttachment storeAttachment(Long taskId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attachment file must not be empty");
    }

    try {
      Files.createDirectories(uploadDirectory);
      String originalFileName = file.getOriginalFilename() == null ? "file" : Path.of(file.getOriginalFilename()).getFileName().toString();
      String storedFileName = UUID.randomUUID() + "-" + originalFileName;
      Path target = uploadDirectory.resolve(storedFileName);

      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
      }

      TaskAttachment attachment = new TaskAttachment();
      attachment.setTask(taskRepository.getReferenceById(taskId));
      attachment.setFileName(originalFileName);
      attachment.setStoredFileName(storedFileName);
      attachment.setContentType(file.getContentType());
      attachment.setSize(file.getSize());
      attachment.setUploadedAt(LocalDateTime.now());
      return attachmentRepository.save(attachment);
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store attachment", exception);
    }
  }

  public TaskAttachment getAttachment(Long attachmentId) {
    return attachmentRepository.findById(attachmentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));
  }

  public Resource loadAsResource(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    Path path = uploadDirectory.resolve(attachment.getStoredFileName()).normalize();
    Resource resource = new FileSystemResource(path);
    if (!resource.exists()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment file not found");
    }
    return resource;
  }

  public void deleteAttachment(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    try {
      Files.deleteIfExists(uploadDirectory.resolve(attachment.getStoredFileName()));
      attachmentRepository.deleteById(attachmentId);
    } catch (IOException exception) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete attachment", exception);
    }
  }

  public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
    return attachmentRepository.findByTask_Id(taskId);
  }

}
