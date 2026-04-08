package com.mipt.todolist.repository;

import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @Test
  void findByCompletedAndPriorityShouldReturnMatchingTasks() {
    taskRepository.save(buildTask("Done high", true, Priority.HIGH, LocalDate.now().plusDays(2)));
    taskRepository.save(buildTask("Open high", false, Priority.HIGH, LocalDate.now().plusDays(3)));
    taskRepository.save(buildTask("Done low", true, Priority.LOW, LocalDate.now().plusDays(4)));

    List<Task> tasks = taskRepository.findByCompletedAndPriority(true, Priority.HIGH);

    assertThat(tasks).hasSize(1);
    assertThat(tasks.getFirst().getTitle()).isEqualTo("Done high");
  }

  @Test
  void findTasksDueBetweenShouldReturnTasksForNearestSevenDays() {
    taskRepository.save(buildTask("Near task", false, Priority.MEDIUM, LocalDate.now().plusDays(5)));
    taskRepository.save(buildTask("Far task", false, Priority.MEDIUM, LocalDate.now().plusDays(10)));

    List<Task> tasks = taskRepository.findTasksDueBetween(LocalDate.now(), LocalDate.now().plusDays(7));

    assertThat(tasks).extracting(Task::getTitle).containsExactly("Near task");
  }

  @Test
  void findAllWithAttachmentsShouldReturnTasksWithAttachments() {
    Task task = taskRepository.save(buildTask("Task with attachment", false, Priority.HIGH, LocalDate.now().plusDays(1)));
    TaskAttachment attachment = new TaskAttachment();
    attachment.setFileName("notes.txt");
    attachment.setStoredFileName("stored-notes.txt");
    attachment.setContentType("text/plain");
    attachment.setSize(100);
    attachment.setUploadedAt(java.time.LocalDateTime.now());
    attachment.setTask(task);
    task.getAttachments().add(attachment);
    taskAttachmentRepository.save(attachment);

    List<Task> tasks = taskRepository.findAllWithAttachments();

    assertThat(tasks).hasSize(1);
    assertThat(tasks.getFirst().getAttachments()).hasSize(1);
    assertThat(tasks.getFirst().getAttachments().iterator().next().getFileName()).isEqualTo("notes.txt");
  }

  private Task buildTask(String title, boolean completed, Priority priority, LocalDate dueDate) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("Task for repository test");
    task.setCompleted(completed);
    task.setPriority(priority);
    task.setDueDate(dueDate);
    task.setTags(Set.of("test"));
    return task;
  }
}
