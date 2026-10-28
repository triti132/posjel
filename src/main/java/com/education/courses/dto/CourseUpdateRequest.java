package com.education.courses.dto;

import com.education.courses.entity.CourseVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(
        @NotBlank(message = "Название курса обязательно")
        @Size(max = 255, message = "Название не должно превышать 255 символов")
        String title,

        @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
        String description,

        String coverUrl,

        String discipline, // <-- ИСПРАВЛЕНО ЗДЕСЬ (было descipline)
        CourseVisibility visibility
) {}