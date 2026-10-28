package com.education.users.dto.response;

public record UserMeResponse(
        Long id,
        String email,
        String fullName,
        String role,
        Boolean isActive
) {
}
