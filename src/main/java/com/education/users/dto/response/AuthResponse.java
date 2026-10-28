package com.education.users.dto.response;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        String role
) {
}
