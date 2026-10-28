package com.education.courses.dto;

import com.education.courses.entity.BlockType;
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
public class LessonBlockUpdateRequest {

    private BlockType blockType;
    private JsonNode content;

    @Positive(message = "Порядковый номер должен быть положительным")
    private Integer orderIndex;
}