package com.education.courses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateRequest {

    @NotBlank(message = "Название урока обязательно")
    private String title;

    private String description;

    @Positive(message = "Порядковый номер должен быть положительным")
    private Integer orderIndex;

    private Boolean isPublished;
}
