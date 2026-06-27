package com.mipt.todolist.repository;

import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void configurePostgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
  }

  @Autowired
  private TaskRepository taskRepository;

  @Test
  void findTasksDueBetweenShouldReturnOnlyTasksInDateOrder() {
    LocalDate startDate = LocalDate.of(2026, 7, 1);
    LocalDate endDate = LocalDate.of(2026, 7, 7);
    taskRepository.saveAll(List.of(
        task("Before range", startDate.minusDays(1)),
        task("Last in range", endDate),
        task("First in range", startDate),
        task("Middle in range", startDate.plusDays(3)),
        task("After range", endDate.plusDays(1))));
    taskRepository.flush();

    List<Task> result = taskRepository.findTasksDueBetween(startDate, endDate);

    assertThat(result)
        .extracting(Task::getTitle)
        .containsExactly("First in range", "Middle in range", "Last in range");
  }

  private Task task(String title, LocalDate dueDate) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("PostgreSQL integration test");
    task.setCompleted(false);
    task.setDueDate(dueDate);
    task.setPriority(Priority.MEDIUM);
    return task;
  }
}
