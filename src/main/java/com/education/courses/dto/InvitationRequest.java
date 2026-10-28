package com.education.courses.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InvitationRequest(
        @NotBlank(message = "Email обязателен")
        @Email(message = "Неккоректный формат email")
        String email
) {
}
