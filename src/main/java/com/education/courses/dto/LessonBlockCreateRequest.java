package com.education.courses.dto;

import com.education.courses.entity.BlockType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode; // <-- ИСПРАВЛЕНО

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonBlockCreateRequest {

    @NotNull(message = "Тип блока обязателен")
    private BlockType blockType;

    @NotNull(message = "Содержимое блока обязательно")
    private JsonNode content;

    @NotNull(message = "Порядковый номер обязателен")
    @Positive(message = "Порядковый номер должен быть положительным")
    private Integer orderIndex;
}