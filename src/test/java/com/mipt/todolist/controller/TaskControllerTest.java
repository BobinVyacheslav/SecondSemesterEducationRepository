package com.mipt.todolist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.todolist.dto.TaskCreateDto;
import com.mipt.todolist.dto.TaskResponseDto;
import com.mipt.todolist.mapper.TaskMapper;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.RequestScopedBean;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TaskService taskService;

  @MockitoBean
  private TaskMapper taskMapper;

  @MockitoBean
  private RequestScopedBean requestScopedBean;

  @Test
  void createShouldReturnCreatedTask() throws Exception {
    TaskCreateDto request = new TaskCreateDto();
    request.setTitle("Write homework");
    request.setDescription("Complete test coverage");
    request.setDueDate(LocalDate.now().plusDays(2));
    request.setPriority(Priority.HIGH);
    request.setTags(Set.of("java", "spring"));

    Task mappedTask = task("Write homework", Priority.HIGH);
    mappedTask.setId(null);
    Task savedTask = task("Write homework", Priority.HIGH);
    TaskResponseDto response = response(savedTask);
    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(mappedTask);
    when(taskService.saveTask(mappedTask)).thenReturn(savedTask);
    when(taskMapper.toResponseDto(savedTask)).thenReturn(response);

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Write homework"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("HIGH"));

    verify(taskService).saveTask(mappedTask);
  }

  @Test
  void getByIdShouldReturnExistingTask() throws Exception {
    Task task = task("Read lecture", Priority.LOW);
    TaskResponseDto response = response(task);
    when(taskService.getTaskById(1L)).thenReturn(Optional.of(task));
    when(taskMapper.toResponseDto(task)).thenReturn(response);

    mockMvc.perform(get("/api/tasks/{id}", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Read lecture"))
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.priority").value("LOW"));
  }

  @Test
  void createShouldRejectInvalidRequest() throws Exception {
    TaskCreateDto request = new TaskCreateDto();
    request.setTitle("Hi");

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.details.title").exists())
        .andExpect(jsonPath("$.details.priority").exists());
  }

  private Task task(String title, Priority priority) {
    Task task = new Task();
    task.setId(1L);
    task.setTitle(title);
    task.setDescription("Task description");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.of(2026, 6, 27, 12, 0));
    task.setDueDate(LocalDate.of(2026, 6, 30));
    task.setPriority(priority);
    task.setTags(Set.of("test"));
    return task;
  }

  private TaskResponseDto response(Task task) {
    TaskResponseDto response = new TaskResponseDto();
    response.setId(task.getId());
    response.setTitle(task.getTitle());
    response.setDescription(task.getDescription());
    response.setCompleted(task.isCompleted());
    response.setCreatedAt(task.getCreatedAt());
    response.setDueDate(task.getDueDate());
    response.setPriority(task.getPriority());
    response.setTags(task.getTags());
    return response;
  }
}
