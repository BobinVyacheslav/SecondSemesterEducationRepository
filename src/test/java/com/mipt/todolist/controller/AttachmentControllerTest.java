package com.mipt.todolist.controller;

import com.mipt.todolist.exception.TaskNotFoundException;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.service.AttachmentService;
import com.mipt.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttachmentController.class)
@Import({ValidationExceptionHandler.class, com.mipt.todolist.config.ApiVersionFilter.class})
@ActiveProfiles("test")
class AttachmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttachmentService attachmentService;

  @MockBean
  private TaskService taskService;

  @Test
  void uploadAttachmentShouldReturnCreated() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "пример.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Текст файла".getBytes(StandardCharsets.UTF_8));
    TaskAttachment attachment = buildAttachment(1L, 3L, "пример.txt");

    when(taskService.getRequiredTask(3L)).thenReturn(new Task());
    when(attachmentService.storeAttachment(eq(3L), any())).thenReturn(attachment);

    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 3L).file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.fileName").value("пример.txt"));
  }

  @Test
  void uploadAttachmentShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "пример.txt", MediaType.TEXT_PLAIN_VALUE, "abc".getBytes());
    when(taskService.getRequiredTask(99L)).thenThrow(new TaskNotFoundException(99L));

    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 99L).file(file))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task with id 99 not found"));
  }

  @Test
  void downloadAttachmentShouldReturnFile() throws Exception {
    TaskAttachment attachment = buildAttachment(1L, 3L, "пример.txt");
    attachment.setContentType("text/plain");
    when(attachmentService.getAttachment(1L)).thenReturn(attachment);
    when(attachmentService.loadAsResource(1L)).thenReturn(new ByteArrayResource("Скачивание".getBytes(StandardCharsets.UTF_8)));

    mockMvc.perform(get("/api/attachments/{attachmentId}", 1L))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("пример.txt")))
        .andExpect(content().bytes("Скачивание".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void downloadAttachmentShouldReturnNotFoundWhenAttachmentDoesNotExist() throws Exception {
    when(attachmentService.getAttachment(20L))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

    mockMvc.perform(get("/api/attachments/{attachmentId}", 20L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Attachment not found"));
  }

  @Test
  void deleteAttachmentShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/attachments/{attachmentId}", 1L))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteAttachmentShouldReturnNotFoundWhenAttachmentDoesNotExist() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"))
        .when(attachmentService).deleteAttachment(11L);

    mockMvc.perform(delete("/api/attachments/{attachmentId}", 11L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Attachment not found"));
  }

  @Test
  void getTaskAttachmentsShouldReturnList() throws Exception {
    when(taskService.getRequiredTask(2L)).thenReturn(new Task());
    when(attachmentService.getAttachmentsByTaskId(2L)).thenReturn(List.of(
        buildAttachment(1L, 2L, "один.txt"),
        buildAttachment(2L, 2L, "два.txt")));

    mockMvc.perform(get("/api/tasks/{taskId}/attachments", 2L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fileName").value("один.txt"))
        .andExpect(jsonPath("$[1].fileName").value("два.txt"));
  }

  @Test
  void getTaskAttachmentsShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
    when(taskService.getRequiredTask(777L)).thenThrow(new TaskNotFoundException(777L));

    mockMvc.perform(get("/api/tasks/{taskId}/attachments", 777L))
        .andExpect(status().isNotFound());
  }

  private TaskAttachment buildAttachment(Long id, Long taskId, String fileName) {
    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(id);
    attachment.setTaskId(taskId);
    attachment.setFileName(fileName);
    attachment.setStoredFileName("stored-" + fileName);
    attachment.setSize(123);
    attachment.setUploadedAt(LocalDateTime.now());
    return attachment;
  }
}
