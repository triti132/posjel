package com.education.tasks.dto;

import com.education.tasks.entity.TaskType;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        Long lessonId,
        TaskType taskType,
        String conditionText,
        LocalDateTime createdAt
) {
}
