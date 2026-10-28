package com.education.tasks.dto;

import com.education.tasks.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskCreateRequest(
        @NotNull TaskType taskType,
        @NotBlank String conditionText,
        @NotBlank String correctAnswer
        ) {
}
