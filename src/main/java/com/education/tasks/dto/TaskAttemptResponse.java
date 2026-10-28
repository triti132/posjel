package com.education.tasks.dto;

import com.education.tasks.entity.AttemptStatus;

public record TaskAttemptResponse(
        Long attemptId,
        AttemptStatus status
) {
}
