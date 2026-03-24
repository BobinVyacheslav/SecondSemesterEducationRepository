package com.mipt.todolist.validation.validator;

import com.mipt.todolist.dto.TaskUpdateDto;
import com.mipt.todolist.model.Task;
import com.mipt.todolist.repository.TaskRepository;
import com.mipt.todolist.validation.annotation.DueDateNotBeforeCreation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

@Component
public class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateDto> {
  private final TaskRepository taskRepository;

  public DueDateNotBeforeCreationValidator(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public boolean isValid(TaskUpdateDto dto, ConstraintValidatorContext constraintValidatorContext) {
    if (dto == null || dto.getDueDate() == null) {
      return true;
    }

    Optional<Long> taskId = resolveTaskIdFromRequest();
    if (taskId.isEmpty()) {
      return true;
    }

    Optional<Task> existingTask = taskRepository.findById(taskId.get());
    if (existingTask.isEmpty() || existingTask.get().getCreatedAt() == null) {
      return true;
    }

    LocalDate creationDate = existingTask.get().getCreatedAt().toLocalDate();
    boolean valid = !dto.getDueDate().isBefore(creationDate);
    if (!valid) {
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext
          .buildConstraintViolationWithTemplate("dueDate must not be before task creation date")
          .addPropertyNode("dueDate")
          .addConstraintViolation();
    }
    return valid;
  }

  @SuppressWarnings("unchecked")
  private Optional<Long> resolveTaskIdFromRequest() {
    if (getRequestAttributes() == null) {
      return Optional.empty();
    }

    Object uriVariables = getRequestAttributes()
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, 0);
    if (!(uriVariables instanceof Map<?, ?> variables)) {
      return Optional.empty();
    }

    Object id = variables.get("id");
    if (id == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(Long.valueOf(id.toString()));
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }
}
