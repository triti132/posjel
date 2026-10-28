package com.education.courses.dto;

import com.education.courses.entity.CourseStatus;
import com.education.courses.entity.CourseVisibility;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String coverUrl,
        String discipline,
        CourseStatus status,
        CourseVisibility visibility,
        Long authorId,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt // <-- ДОБАВЛЕНО ЗДЕСЬ
) {}