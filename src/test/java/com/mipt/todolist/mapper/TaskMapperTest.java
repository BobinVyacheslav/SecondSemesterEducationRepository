package com.mipt.todolist.mapper;

import com.mipt.todolist.dto.TaskCreateDto;
import com.mipt.todolist.dto.TaskResponseDto;
import com.mipt.todolist.dto.TaskUpdateDto;
import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

  private final TaskMapper taskMapper = new TaskMapperImpl();

  @Test
  void toEntityShouldMapCreateDtoToTask() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("мтс дз");
    dto.setDescription("Подготовить код");
    dto.setDueDate(LocalDate.now().plusDays(2));
    dto.setPriority(Priority.HIGH);
    dto.setTags(Set.of("java", "spring"));

    Task task = taskMapper.toEntity(dto);

    assertThat(task.getTitle()).isEqualTo("мтс дз");
    assertThat(task.getDescription()).isEqualTo("Подготовить код");
    assertThat(task.getDueDate()).isEqualTo(dto.getDueDate());
    assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(task.getTags()).containsExactlyInAnyOrder("java", "spring");
    assertThat(task.getId()).isNull();
    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getCreatedAt()).isNull();
  }

  @Test
  void updateEntityShouldChangeOnlyPassedFields() {
    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setTitle("Новое название");
    dto.setCompleted(true);

    Task task = new Task();
    task.setId(10L);
    task.setTitle("Старое название");
    task.setDescription("Старое описание");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now().minusDays(1));
    task.setDueDate(LocalDate.now().plusDays(5));
    task.setPriority(Priority.MEDIUM);
    task.setTags(Set.of("учеба"));

    taskMapper.updateEntity(dto, task);

    assertThat(task.getTitle()).isEqualTo("Новое название");
    assertThat(task.getDescription()).isEqualTo("Старое описание");
    assertThat(task.isCompleted()).isTrue();
    assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(task.getCreatedAt()).isNotNull();
  }

  @Test
  void toResponseDtoShouldMapTaskToResponseDto() {
    Task task = new Task();
    task.setId(3L);
    task.setTitle("Прочитать лекцию");
    task.setDescription("Лекция 5");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(1));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("весна"));

    TaskResponseDto dto = taskMapper.toResponseDto(task);

    assertThat(dto.getId()).isEqualTo(3L);
    assertThat(dto.getTitle()).isEqualTo("Прочитать лекцию");
    assertThat(dto.getDescription()).isEqualTo("Лекция 5");
    assertThat(dto.isCompleted()).isFalse();
    assertThat(dto.getPriority()).isEqualTo(Priority.LOW);
    assertThat(dto.getTags()).containsExactly("весна");
  }
}
