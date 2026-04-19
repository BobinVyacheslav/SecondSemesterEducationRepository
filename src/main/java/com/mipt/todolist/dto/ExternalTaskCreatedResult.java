package com.mipt.todolist.dto;

import java.net.URI;

public record ExternalTaskCreatedResult(ExternalTaskResponse task, URI location) {
}
