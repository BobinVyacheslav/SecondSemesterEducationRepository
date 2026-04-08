package com.mipt.todolist.service;

import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.repository.TaskAttachmentRepository;
import com.mipt.todolist.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void storeAttachmentShouldSaveFileAndMetadata() throws Exception {
    TaskAttachmentRepository attachmentRepository = mock(TaskAttachmentRepository.class);
    TaskRepository taskRepository = mock(TaskRepository.class);
    AttachmentService service = new AttachmentService(attachmentRepository, taskRepository, tempDir.toString());

    Task task = new Task();
    task.setId(1L);
    when(taskRepository.getReferenceById(1L)).thenReturn(task);
    when(attachmentRepository.save(any(TaskAttachment.class))).thenAnswer(invocation -> {
      TaskAttachment attachment = invocation.getArgument(0);
      attachment.setId(10L);
      return attachment;
    });

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "konspekt.txt",
        "text/plain",
        "Soderzhimoe faila".getBytes(StandardCharsets.UTF_8));

    TaskAttachment attachment = service.storeAttachment(1L, file);

    assertThat(attachment.getId()).isPositive();
    assertThat(attachment.getTaskId()).isEqualTo(1L);
    assertThat(attachment.getFileName()).isEqualTo("konspekt.txt");
    assertThat(attachment.getStoredFileName()).contains("konspekt.txt");
    assertThat(attachment.getUploadedAt()).isNotNull();
    assertThat(Files.exists(tempDir.resolve(attachment.getStoredFileName()))).isTrue();
  }

  @Test
  void storeAttachmentShouldThrowWhenFileIsEmpty() {
    AttachmentService service = new AttachmentService(
        mock(TaskAttachmentRepository.class),
        mock(TaskRepository.class),
        tempDir.toString());
    MockMultipartFile emptyFile = new MockMultipartFile("file", "pustoy.txt", "text/plain", new byte[0]);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.storeAttachment(1L, emptyFile));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void loadAsResourceShouldReturnSavedFile() throws Exception {
    TaskAttachmentRepository attachmentRepository = mock(TaskAttachmentRepository.class);
    AttachmentService service = new AttachmentService(attachmentRepository, mock(TaskRepository.class), tempDir.toString());

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "file.txt",
        "text/plain",
        "Proverka zagruzki".getBytes(StandardCharsets.UTF_8));
    TaskAttachment storedAttachment = new TaskAttachment();
    storedAttachment.setId(2L);
    storedAttachment.setFileName("file.txt");
    storedAttachment.setStoredFileName("stored-file.txt");
    storedAttachment.setUploadedAt(java.time.LocalDateTime.now());

    Files.writeString(tempDir.resolve("stored-file.txt"), "Proverka zagruzki", StandardCharsets.UTF_8);
    when(attachmentRepository.findById(2L)).thenReturn(Optional.of(storedAttachment));

    Resource resource = service.loadAsResource(2L);

    assertThat(file.getOriginalFilename()).isEqualTo("file.txt");
    assertThat(resource.exists()).isTrue();
    assertThat(resource.getFilename()).contains("stored-file.txt");
  }

  @Test
  void getAttachmentShouldThrowWhenAttachmentDoesNotExist() {
    TaskAttachmentRepository attachmentRepository = mock(TaskAttachmentRepository.class);
    when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());
    AttachmentService service = new AttachmentService(attachmentRepository, mock(TaskRepository.class), tempDir.toString());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.getAttachment(999L));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteAttachmentShouldRemoveMetadataAndFile() throws Exception {
    TaskAttachmentRepository attachmentRepository = mock(TaskAttachmentRepository.class);
    AttachmentService service = new AttachmentService(attachmentRepository, mock(TaskRepository.class), tempDir.toString());

    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(3L);
    attachment.setFileName("delete.txt");
    attachment.setStoredFileName("stored-delete.txt");
    attachment.setUploadedAt(java.time.LocalDateTime.now());

    Path storedPath = tempDir.resolve("stored-delete.txt");
    Files.writeString(storedPath, "Nado udalit", StandardCharsets.UTF_8);
    when(attachmentRepository.findById(3L)).thenReturn(Optional.of(attachment), Optional.empty());
    doAnswer(invocation -> null).when(attachmentRepository).deleteById(3L);

    service.deleteAttachment(3L);

    assertThat(Files.exists(storedPath)).isFalse();
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.getAttachment(3L));
    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
