package com.mipt.todolist.service;

import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.repository.TaskAttachmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttachmentServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void storeAttachmentShouldSaveFileAndMetadata() throws Exception {
    AttachmentService service = new AttachmentService(new TaskAttachmentRepository(), tempDir.toString());
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "конспект.txt",
        "text/plain",
        "Содержимое файла".getBytes(StandardCharsets.UTF_8));

    TaskAttachment attachment = service.storeAttachment(1L, file);

    assertThat(attachment.getId()).isPositive();
    assertThat(attachment.getTaskId()).isEqualTo(1L);
    assertThat(attachment.getFileName()).isEqualTo("конспект.txt");
    assertThat(attachment.getStoredFileName()).contains("конспект.txt");
    assertThat(attachment.getUploadedAt()).isNotNull();
    assertThat(Files.exists(tempDir.resolve(attachment.getStoredFileName()))).isTrue();
  }

  @Test
  void storeAttachmentShouldThrowWhenFileIsEmpty() {
    AttachmentService service = new AttachmentService(new TaskAttachmentRepository(), tempDir.toString());
    MockMultipartFile emptyFile = new MockMultipartFile("file", "пустой.txt", "text/plain", new byte[0]);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.storeAttachment(1L, emptyFile));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void loadAsResourceShouldReturnSavedFile() throws Exception {
    AttachmentService service = new AttachmentService(new TaskAttachmentRepository(), tempDir.toString());
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "файл.txt",
        "text/plain",
        "Проверка загрузки".getBytes(StandardCharsets.UTF_8));
    TaskAttachment attachment = service.storeAttachment(2L, file);

    Resource resource = service.loadAsResource(attachment.getId());

    assertThat(resource.exists()).isTrue();
    assertThat(resource.getFilename()).contains("файл.txt");
  }

  @Test
  void getAttachmentShouldThrowWhenAttachmentDoesNotExist() {
    AttachmentService service = new AttachmentService(new TaskAttachmentRepository(), tempDir.toString());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.getAttachment(999L));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteAttachmentShouldRemoveMetadataAndFile() {
    AttachmentService service = new AttachmentService(new TaskAttachmentRepository(), tempDir.toString());
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "удалить.txt",
        "text/plain",
        "Надо удалить".getBytes(StandardCharsets.UTF_8));
    TaskAttachment attachment = service.storeAttachment(3L, file);
    Path storedPath = tempDir.resolve(attachment.getStoredFileName());

    service.deleteAttachment(attachment.getId());

    assertThat(Files.exists(storedPath)).isFalse();
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> service.getAttachment(attachment.getId()));
    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
