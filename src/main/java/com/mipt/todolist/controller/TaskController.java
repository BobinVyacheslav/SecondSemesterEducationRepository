package com.mipt.todolist.controller;

import com.mipt.todolist.model.RequestScopedBean;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Основной REST-контроллер для управления задачами.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;
  private final RequestScopedBean requestScopedBean;

  /**
   * Конструктор с внедрением сервиса и бина области запроса.
   * @param taskService сервис задач.
   * @param requestScopedBean бин текущего запроса.
   */
  public TaskController(TaskService taskService, RequestScopedBean requestScopedBean) {
    this.taskService = taskService;
    this.requestScopedBean = requestScopedBean;
  }

  /**
   * Получает список всех задач.
   * @return список задач и HTTP 200.
   */
  @GetMapping
  public List<Task> getAll() {
    System.out.println("Обработка запроса: " + requestScopedBean.getRequestId());
    return taskService.getAllTasks();
  }

  /**
   * Возвращает задачу по идентификатору.
   * @param id уникальный ключ задачи.
   * @return задача или 404 если не найдена.
   */
  @GetMapping("/{id}")
  public ResponseEntity<Task> getById(@PathVariable String id) {
    return taskService.getTaskById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Создает новую задачу.
   * @param task данные задачи.
   * @return созданная задача с ID.
   */
  @PostMapping
  public Task create(@RequestBody Task task) {
    return taskService.saveTask(task);
  }

  /**
   * Обновляет существующую задачу.
   * @param id идентификатор задачи.
   * @param task новые данные.
   * @return обновленная задача или 404.
   */
  @PutMapping("/{id}")
  public ResponseEntity<Task> update(@PathVariable String id, @RequestBody Task task) {
    if (taskService.getTaskById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    task.setId(id);
    return ResponseEntity.ok(taskService.saveTask(task));
  }

  /**
   * Удаляет задачу из системы.
   * @param id идентификатор задачи.
   * @return 204 No Content или 404.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    if (taskService.getTaskById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }
}