package com.mipt.todolist.dto;

public record ExternalTaskResponse(Long id, String title, String description, boolean completed) {
}
