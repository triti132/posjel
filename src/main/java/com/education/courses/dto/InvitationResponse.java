package com.education.courses.dto;

import com.education.courses.entity.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        String email,
        InvitationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
