package com.mipt.todolist.repository;

import com.mipt.todolist.model.Priority;
import com.mipt.todolist.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы с задачами через Spring Data JPA.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

  @Query("""
      select t
      from Task t
      where t.dueDate between :startDate and :endDate
      order by t.dueDate asc
      """)
  List<Task> findTasksDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @EntityGraph(attributePaths = "attachments")
  @Query("select t from Task t")
  List<Task> findAllWithAttachments();
}
