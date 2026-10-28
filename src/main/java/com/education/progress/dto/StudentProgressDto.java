package com.education.progress.dto;

public record StudentProgressDto(
        Long userId,
        String fullName,
        String email,
        long completedLessonsCount,
        long totalPublishedLessonsCount,
        double progressPercentage
) {
}
