package com.mipt.todolist.repository;

import com.mipt.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с задачами через Spring Data JPA.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
