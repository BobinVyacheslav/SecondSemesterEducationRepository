package com.mipt.todolist.service;

import com.mipt.todolist.model.Task;
import com.mipt.todolist.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskServiceTest {

  @Autowired
  private TaskService taskService;

  @MockitoBean
  private TaskRepository taskRepository;

  @Test
  void bulkCompleteTasksShouldCompleteExistingTask() {
    Task task = new Task(1L, "Write tests", "Complete homework 5", false);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    taskService.bulkCompleteTasks(List.of(1L));

    verify(taskRepository).findById(1L);
    ArgumentCaptor<Iterable<Task>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(taskRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
        .singleElement()
        .satisfies(savedTask -> {
          assertThat(savedTask.getId()).isEqualTo(1L);
          assertThat(savedTask.isCompleted()).isTrue();
        });
  }
}
