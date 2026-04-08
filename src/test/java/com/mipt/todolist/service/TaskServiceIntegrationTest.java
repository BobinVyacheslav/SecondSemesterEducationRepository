package com.mipt.todolist.service;

import com.mipt.todolist.exception.BulkTaskCompletionException;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import com.mipt.todolist.repository.TaskAttachmentRepository;
import com.mipt.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @BeforeEach
  void clearData() {
    taskAttachmentRepository.deleteAll();
    taskRepository.deleteAll();
  }

  @Test
  void bulkCompleteTasksShouldMarkAllTasksAsCompleted() {
    Task firstTask = taskRepository.save(buildTask("First task"));
    Task secondTask = taskRepository.save(buildTask("Second task"));

    taskService.bulkCompleteTasks(List.of(firstTask.getId(), secondTask.getId()));

    assertThat(taskRepository.findById(firstTask.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
    assertThat(taskRepository.findById(secondTask.getId())).get().extracting(Task::isCompleted).isEqualTo(true);
  }

  @Test
  void bulkCompleteTasksShouldRollbackWhenTaskDoesNotExist() {
    Task firstTask = taskRepository.save(buildTask("First task"));
    Task secondTask = taskRepository.save(buildTask("Second task"));

    assertThrows(BulkTaskCompletionException.class,
        () -> taskService.bulkCompleteTasks(List.of(firstTask.getId(), 999999L, secondTask.getId())));

    assertThat(taskRepository.findById(firstTask.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
    assertThat(taskRepository.findById(secondTask.getId())).get().extracting(Task::isCompleted).isEqualTo(false);
  }

  @Test
  void getAllTasksWithAttachmentsShouldReturnTasksWithLoadedAttachments() {
    Task task = taskRepository.save(buildTask("Task with file"));

    TaskAttachment attachment = new TaskAttachment();
    attachment.setTask(task);
    attachment.setFileName("lecture.pdf");
    attachment.setStoredFileName("stored-lecture.pdf");
    attachment.setContentType("application/pdf");
    attachment.setSize(1234);
    attachment.setUploadedAt(LocalDateTime.now());
    task.getAttachments().add(attachment);
    taskAttachmentRepository.save(attachment);

    List<Task> tasks = taskService.getAllTasksWithAttachments();

    assertThat(tasks).hasSize(1);
    assertThat(tasks.getFirst().getAttachments()).hasSize(1);
    assertThat(tasks.getFirst().getAttachments().iterator().next().getFileName()).isEqualTo("lecture.pdf");
  }

  private Task buildTask(String title) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("Task for service integration test");
    task.setCompleted(false);
    task.setPriority(Priority.MEDIUM);
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setTags(Set.of("integration"));
    return task;
  }
}
