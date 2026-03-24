package com.mipt.todolist.controller;

import com.mipt.todolist.dto.TaskResponseDto;
import com.mipt.todolist.mapper.TaskMapper;
import com.mipt.todolist.service.FavoritesService;
import com.mipt.todolist.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Favorites stored in HTTP session")
public class FavoritesController {
  private final FavoritesService favoritesService;
  private final TaskService taskService;
  private final TaskMapper taskMapper;

  public FavoritesController(FavoritesService favoritesService, TaskService taskService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskService = taskService;
    this.taskMapper = taskMapper;
  }

  @PostMapping("/{taskId}")
  @Operation(summary = "Add task to favorites")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Task added to favorites"),
      @ApiResponse(responseCode = "404", description = "Task not found")
  })
  public ResponseEntity<Void> addFavorite(@PathVariable Long taskId, HttpSession session) {
    taskService.getRequiredTask(taskId);
    favoritesService.addFavorite(taskId, session);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{taskId}")
  @Operation(summary = "Remove task from favorites")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Task removed from favorites")
  })
  public ResponseEntity<Void> removeFavorite(@PathVariable Long taskId, HttpSession session) {
    favoritesService.removeFavorite(taskId, session);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get favorite tasks")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Favorite task list returned")
  })
  public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
    List<TaskResponseDto> favorites = new java.util.ArrayList<>();
    for (Long taskId : favoritesService.getFavoriteTaskIds(session)) {
      favorites.add(taskMapper.toResponseDto(taskService.getRequiredTask(taskId)));
    }
    return ResponseEntity.ok(favorites);
  }
}
