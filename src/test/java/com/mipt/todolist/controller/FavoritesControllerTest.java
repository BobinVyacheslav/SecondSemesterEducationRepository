package com.mipt.todolist.controller;

import com.mipt.todolist.dto.TaskResponseDto;
import com.mipt.todolist.exception.GlobalExceptionHandler;
import com.mipt.todolist.exception.TaskNotFoundException;
import com.mipt.todolist.mapper.TaskMapper;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.service.FavoritesService;
import com.mipt.todolist.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoritesController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, com.mipt.todolist.config.ApiVersionFilter.class})
@ActiveProfiles("test")
class FavoritesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FavoritesService favoritesService;

  @MockBean
  private TaskService taskService;

  @MockBean
  private TaskMapper taskMapper;

  @Test
  void addFavoriteShouldReturnOk() throws Exception {
    when(taskService.getRequiredTask(5L)).thenReturn(new Task());
    doNothing().when(favoritesService).addFavorite(eq(5L), any());

    mockMvc.perform(post("/api/favorites/{taskId}", 5L))
        .andExpect(status().isOk());
  }

  @Test
  void addFavoriteShouldReturnNotFoundWhenTaskMissing() throws Exception {
    when(taskService.getRequiredTask(55L)).thenThrow(new TaskNotFoundException(55L));

    mockMvc.perform(post("/api/favorites/{taskId}", 55L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Task with id 55 not found"));
  }

  @Test
  void removeFavoriteShouldReturnNoContent() throws Exception {
    doNothing().when(favoritesService).removeFavorite(eq(8L), any());

    mockMvc.perform(delete("/api/favorites/{taskId}", 8L))
        .andExpect(status().isNoContent());
  }

  @Test
  void getFavoritesShouldReturnMappedTasks() throws Exception {
    MockHttpSession session = new MockHttpSession();
    Task task = new Task();
    task.setId(3L);
    task.setTitle("Избранное");
    TaskResponseDto dto = new TaskResponseDto();
    dto.setId(3L);
    dto.setTitle("Избранное");

    when(favoritesService.getFavoriteTaskIds(session)).thenReturn(Set.of(3L));
    when(taskService.getRequiredTask(3L)).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(dto);

    mockMvc.perform(get("/api/favorites").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(3))
        .andExpect(jsonPath("$[0].title").value("Избранное"));
  }

  @Test
  void getFavoritesShouldReturnEmptyListWhenSessionHasNoFavorites() throws Exception {
    MockHttpSession session = new MockHttpSession();
    when(favoritesService.getFavoriteTaskIds(session)).thenReturn(Set.of());

    mockMvc.perform(get("/api/favorites").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }
}
