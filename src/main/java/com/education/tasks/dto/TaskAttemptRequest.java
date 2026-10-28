package com.education.tasks.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskAttemptRequest(
        String studentAnswer
) {
}
