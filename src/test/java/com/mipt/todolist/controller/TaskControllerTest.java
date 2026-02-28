package com.mipt.todolist.controller;

import com.mipt.todolist.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты, покрывающие все CRUD-операции контроллера задач.
 * Проверяется как успешное выполнение, так и обработка ошибок.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Позитивный тест: получение всех задач.
   */
  @Test
  void getAll_Positive_ReturnsList() {
    ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks", Task[].class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  /**
   * Негативный тест: имитация ошибки (в данном MVP проверяем пустой путь или неверный формат)
   */
  @Test
  void getAll_Negative_InvalidEndpoint() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/undefined/route", String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /**
   * Позитивный тест: поиск существующей задачи по ID
   */
  @Test
  void getById_Positive_Found() {
    Task created = restTemplate.postForObject("/api/tasks", new Task("first", "Find Me", "Desc", false), Task.class);
    ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/" + created.getId(), Task.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Find Me", response.getBody().getTitle());
  }

  /**
   * Негативный тест: поиск задачи по несуществующему ID.
   */
  @Test
  void getById_Negative_NotFound() {
    ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/ghost-id", Task.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /**
   * Позитивный тест: создание новой задачи.
   */
  @Test
  void create_Positive_Created() {
    Task task = new Task("second", "New Task", "Description", false);
    ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", task, Task.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().getId());
  }

  /**
   * Более подробный позитивный тест на создание новой задачи
   */

  @Test
  void create_ShouldReturnCreatedAndPersist() {
    Task task = new Task("second", "New Task", "Desc", false);

    ResponseEntity<Task> response =
        restTemplate.postForEntity("/api/tasks", task, Task.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().getId());

    ResponseEntity<Task> stored =
        restTemplate.getForEntity("/api/tasks/" + response.getBody().getId(), Task.class);

    assertEquals(HttpStatus.OK, stored.getStatusCode());
  }

  /**
   * Негативный тест: попытка создания задачи с некорректным телом (null).
   */
  @Test
  void create_Negative_NullBody() {
    ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", null, Task.class);
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
  }

  /**
   * Позитивный тест: обновление существующей задачи.
   */
  @Test
  void update_Positive_Success() {
    Task saved = restTemplate.postForObject("/api/tasks", new Task("third", "Old", "D", false), Task.class);
    saved.setTitle("Updated Title");
    HttpEntity<Task> request = new HttpEntity<>(saved);
    ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/" + saved.getId(), HttpMethod.PUT, request, Task.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Updated Title", response.getBody().getTitle());
  }

  /**
   * Негативный тест: обновление задачи, которой нет в базе.
   */
  @Test
  void update_Negative_NotFound() {
    Task task = new Task("fake-id", "Title", "Desc", false);
    HttpEntity<Task> request = new HttpEntity<>(task);
    ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/fake-id", HttpMethod.PUT, request, Task.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /**
   * Позитивный тест: удаление существующей задачи.
   */
  @Test
  void delete_Positive_NoContent() {
    Task saved = restTemplate.postForObject("/api/tasks", new Task("forth", "To Delete", "D", false), Task.class);
    ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/" + saved.getId(), HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  /**
   * Негативный тест: удаление задачи по несуществующему ID.
   */
  @Test
  void delete_Negative_NotFound() {
    ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/not-found-id", HttpMethod.DELETE, null, Void.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}