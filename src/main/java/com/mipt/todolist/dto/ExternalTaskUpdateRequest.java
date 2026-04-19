package com.mipt.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExternalTaskUpdateRequest(
    @NotBlank String title,
    String description,
    @NotNull Boolean completed) {
}
