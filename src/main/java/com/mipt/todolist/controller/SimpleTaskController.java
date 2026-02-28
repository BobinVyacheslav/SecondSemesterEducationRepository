//package com.mipt.todolist.controller;
//
//import com.mipt.todolist.model.Task;
//import com.mipt.todolist.service.TaskService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping
//public class SimpleTaskController {
//
//  private final TaskService taskService;
//
//  /**
//   * Внедрение зависимости через конструктор
//   * Spring автоматически передает сюда TaskService
//   */
//  public SimpleTaskController(TaskService taskService) {
//    this.taskService = taskService;
//  }
//
//  @GetMapping
//  public List<Task> getAll() {
//    return taskService.getAllTasks();
//  }
//
//  @PostMapping
//  public Task create(@RequestBody Task task) {
//    return taskService.saveTask(task);
//  }
//
//  @GetMapping("/{id}")
//  public Task getById(@PathVariable String id) {
//    return taskService.getTaskById(id).orElse(null);
//  }
//
//  @DeleteMapping("/{id}")
//  public void delete(@PathVariable String id) {
//    taskService.deleteTask(id);
//  }
//}