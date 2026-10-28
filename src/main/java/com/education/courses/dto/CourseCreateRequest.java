package com.education.courses.dto;

import com.education.courses.entity.CourseVisibility;
import jakarta.validation.constraints.NotBlank;

public record CourseCreateRequest(
        @NotBlank(message = "Название курса обязательно")
        String title,

        String description,
        String coverUrl,
        String discipline,

        CourseVisibility visibility
) {}