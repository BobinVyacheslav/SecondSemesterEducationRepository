package com.mipt.todolist.dto;

import jakarta.validation.constraints.NotBlank;

public record ExternalTaskCreateRequest(@NotBlank String title, String description) {
}
