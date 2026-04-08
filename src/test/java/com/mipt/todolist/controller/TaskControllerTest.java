package com.mipt.todolist.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.TaskCreateDto;
import com.mipt.todolist.dto.TaskUpdateDto;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TaskRepository taskRepository;

  @BeforeEach
  void resetRepository() {
    taskRepository.deleteAll();
  }

  @Test
  void createShouldReturnResponseDtoWithGeneratedIdAndCreatedAt() throws Exception {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Write homework");
    dto.setDescription("sleep");
    dto.setDueDate(LocalDate.now().plusDays(2));
    dto.setPriority(Priority.HIGH);
    dto.setTags(Set.of("java", "spring"));

    String responseBody = mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.title").value("Write homework"))
        .andExpect(jsonPath("$.description").value("sleep"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("HIGH"))
        .andExpect(jsonPath("$.createdAt").exists())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertThat(jsonNode.get("id").asLong()).isPositive();
    assertThat(LocalDateTime.parse(jsonNode.get("createdAt").asText())).isNotNull();
  }

  @Test
  void createShouldRejectInvalidDtoAndReturnFieldDetails() throws Exception {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Hi");
    dto.setDescription("x".repeat(501));
    dto.setDueDate(LocalDate.now().minusDays(1));

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.details.title").exists())
        .andExpect(jsonPath("$.details.description").exists())
        .andExpect(jsonPath("$.details.dueDate").exists())
        .andExpect(jsonPath("$.details.priority").exists());
  }

  @Test
  void updateShouldOnlyChangeProvidedFields() throws Exception {
    Task existing = new Task();
    existing.setTitle("Original title");
    existing.setDescription("Original description");
    existing.setCompleted(false);
    existing.setCreatedAt(LocalDateTime.now().minusDays(2));
    existing.setDueDate(LocalDate.now().plusDays(5));
    existing.setPriority(Priority.MEDIUM);
    existing.setTags(Set.of("study"));
    Task saved = taskRepository.save(existing);

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setTitle("Updated title");

    mockMvc.perform(put("/api/tasks/{id}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated title"))
        .andExpect(jsonPath("$.description").value("Original description"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("MEDIUM"))
        .andExpect(jsonPath("$.tags[0]").value("study"));
  }

  @Test
  void updateShouldRejectDueDateBeforeCreationDate() throws Exception {
    Task existing = new Task();
    existing.setTitle("Original title");
    existing.setDescription("Original description");
    existing.setCompleted(false);
    existing.setCreatedAt(LocalDateTime.now().plusDays(2));
    existing.setDueDate(LocalDate.now().plusDays(10));
    existing.setPriority(Priority.MEDIUM);
    existing.setTags(Set.of("study"));
    Task saved = taskRepository.save(existing);

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setDueDate(LocalDate.now().plusDays(1));

    mockMvc.perform(put("/api/tasks/{id}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.dueDate").value("dueDate must not be before task creation date"));
  }

  @Test
  void getByIdShouldReturnResponseDto() throws Exception {
    Task task = new Task();
    task.setTitle("Read lecture");
    task.setDescription("Lecture 4 and 5");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("lecture"));
    Task saved = taskRepository.save(task);

    mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(saved.getId()))
        .andExpect(jsonPath("$.title").value("Read lecture"))
        .andExpect(jsonPath("$.priority").value("LOW"))
        .andExpect(jsonPath("$.createdAt").exists());
  }
}
