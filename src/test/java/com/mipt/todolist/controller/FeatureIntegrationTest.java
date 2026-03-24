package com.mipt.todolist.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.repository.TaskAttachmentRepository;
import com.mipt.todolist.repository.TaskRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  @BeforeEach
  void resetState() throws Exception {
    attachmentRepository.findAll().stream()
        .map(TaskAttachment::getId)
        .forEach(attachmentRepository::deleteById);

    taskRepository.findAll().stream()
        .map(Task::getId)
        .forEach(taskRepository::deleteById);

    Path uploadsPath = Path.of("uploads");
    if (Files.exists(uploadsPath)) {
      try (var files = Files.walk(uploadsPath)) {
        files.sorted(Comparator.reverseOrder())
            .forEach(path -> {
              try {
                Files.deleteIfExists(path);
              } catch (Exception ignored) {
              }
            });
      }
    }
  }

  @Test
  void tasksListShouldExposeRequiredHeaders() throws Exception {
    taskRepository.save(buildTask("Первая задача"));
    taskRepository.save(buildTask("Вторая задача"));

    mockMvc.perform(get("/api/tasks").header("Origin", "http://localhost:3000"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "2"))
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(header().string("Access-Control-Expose-Headers", org.hamcrest.Matchers.containsString("X-Total-Count")));
  }

  @Test
  void attachmentEndpointsShouldUploadListDownloadAndDelete() throws Exception {
    Task savedTask = taskRepository.save(buildTask("Задача с файлом"));
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "einstein_files.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Пример содержимого файла".getBytes());

    String uploadResponse = mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", savedTask.getId()).file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fileName").value("einstein_files.txt"))
        .andExpect(jsonPath("$.uploadedAt").exists())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long attachmentId = objectMapper.readTree(uploadResponse).get("id").asLong();

    mockMvc.perform(get("/api/tasks/{taskId}/attachments", savedTask.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(attachmentId))
        .andExpect(jsonPath("$[0].fileName").value("einstein_files.txt"));

    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("einstein_files.txt")))
        .andExpect(header().string("X-API-Version", "2.0.0"))
        .andExpect(result -> assertThat(new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
            .isEqualTo("Пример содержимого файла"));

    mockMvc.perform(delete("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
        .andExpect(status().isNotFound());
  }

  @Test
  void favoritesShouldUseSessionStorage() throws Exception {
    Task savedTask = taskRepository.save(buildTask("Избранная задача"));
    MockHttpSession session = new MockHttpSession();

    mockMvc.perform(post("/api/favorites/{taskId}", savedTask.getId()).session(session))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/favorites").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(savedTask.getId()))
        .andExpect(jsonPath("$[0].title").value("Избранная задача"));

    mockMvc.perform(delete("/api/favorites/{taskId}", savedTask.getId()).session(session))
        .andExpect(status().isNoContent());
  }

  @Test
  void preferencesShouldReadAndUpdateCookieValue() throws Exception {
    mockMvc.perform(get("/api/preferences/view"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "compact"))
        .andExpect(jsonPath("$.mode").value("compact"));

    mockMvc.perform(post("/api/preferences/view").param("mode", "detailed"))
        .andExpect(status().isOk())
        .andExpect(cookie().value("viewPreference", "detailed"))
        .andExpect(jsonPath("$.mode").value("detailed"));

    mockMvc.perform(get("/api/preferences/view").cookie(new Cookie("viewPreference", "detailed")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mode").value("detailed"));
  }

  @Test
  void noHandlerAndCorsAndOpenApiShouldBeAvailable() throws Exception {
    mockMvc.perform(get("/api/unknown-endpoint"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.path").value("/api/unknown-endpoint"));

    mockMvc.perform(options("/api/tasks")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

    String apiDocs = mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    JsonNode docs = objectMapper.readTree(apiDocs);
    assertThat(docs.path("info").path("title").asText()).isEqualTo("To-Do List API");

    mockMvc.perform(get("/swagger-ui.html"))
        .andExpect(status().is3xxRedirection());
  }

  private Task buildTask(String title) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("Описание для задачи: " + title);
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setPriority(Priority.MEDIUM);
    task.setTags(Set.of("учеба"));
    return task;
  }
}
