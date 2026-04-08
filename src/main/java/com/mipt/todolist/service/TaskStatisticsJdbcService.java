package com.mipt.todolist.service;

import com.mipt.todolist.dto.TaskCountByPriorityDto;
import com.mipt.todolist.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatisticsJdbcService {
  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<TaskCountByPriorityDto> countByPriorityRowMapper = (resultSet, rowNum) ->
      new TaskCountByPriorityDto(
          Priority.valueOf(resultSet.getString("priority")),
          resultSet.getLong("tasks_count"));

  public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<TaskCountByPriorityDto> getTasksCountByPriority() {
    String sql = """
        select priority, count(*) as tasks_count
        from tasks
        group by priority
        order by priority
        """;

    return jdbcTemplate.query(sql, countByPriorityRowMapper);
  }
}
