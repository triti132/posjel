package com.education.users.dto.response;

import com.education.users.entity.Role;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role
) {
}
