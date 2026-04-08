package com.mipt.todolist.service;

import com.mipt.todolist.dto.TaskCountByPriorityDto;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskStatisticsJdbcServiceTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskStatisticsJdbcService taskStatisticsJdbcService;

  @BeforeEach
  void clearRepository() {
    taskRepository.deleteAll();
  }

  @Test
  void getTasksCountByPriorityShouldReturnGroupedStatistics() {
    taskRepository.save(buildTask("High 1", Priority.HIGH));
    taskRepository.save(buildTask("High 2", Priority.HIGH));
    taskRepository.save(buildTask("Low 1", Priority.LOW));

    List<TaskCountByPriorityDto> statistics = taskStatisticsJdbcService.getTasksCountByPriority();

    assertThat(statistics)
        .contains(new TaskCountByPriorityDto(Priority.HIGH, 2))
        .contains(new TaskCountByPriorityDto(Priority.LOW, 1));
  }

  private Task buildTask(String title, Priority priority) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("Task for jdbc statistics");
    task.setCompleted(false);
    task.setPriority(priority);
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setTags(Set.of("stats"));
    return task;
  }
}
