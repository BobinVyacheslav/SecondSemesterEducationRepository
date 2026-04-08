package com.mipt.todolist.dto;

import com.mipt.todolist.model.Priority;

public record TaskCountByPriorityDto(Priority priority, long count) {
}
