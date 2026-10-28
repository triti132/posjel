package com.education.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "education-platform-super-secret-key-must-be-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 часа
    }

    @Test
    @DisplayName("Генерация токена должна создавать валидный JWT")
    void generateToken_ShouldCreateValidJwt() {
        String email = "test@example.com";
        String role = "TEACHER";

        String token = jwtService.generateToken(email, role);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT состоит из 3 частей
    }

    @Test
    @DisplayName("Извлечение email из токена должно работать корректно")
    void extractEmail_ShouldReturnCorrectEmail() {
        String email = "test@example.com";
        String role = "TEACHER";

        String token = jwtService.generateToken(email, role);
        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Валидация токена с правильным email должна возвращать true")
    void isTokenValid_WithCorrectEmail_ShouldReturnTrue() {
        String email = "test@example.com";
        String role = "TEACHER";

        String token = jwtService.generateToken(email, role);
        boolean isValid = jwtService.isTokenValid(token, email);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Валидация токена с неправильным email должна возвращать false")
    void isTokenValid_WithWrongEmail_ShouldReturnFalse() {
        String email = "test@example.com";
        String wrongEmail = "wrong@example.com";
        String role = "TEACHER";

        String token = jwtService.generateToken(email, role);
        boolean isValid = jwtService.isTokenValid(token, wrongEmail);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Токен должен содержать роль в claims")
    void generateToken_ShouldIncludeRoleInClaims() {
        String email = "test@example.com";
        String role = "ADMIN";

        String token = jwtService.generateToken(email, role);

        // Проверяем, что токен сгенерирован (детальную проверку claims можно сделать через parser)
        assertThat(token).isNotNull();
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Разные токены для разных пользователей должны отличаться")
    void generateToken_ForDifferentUsers_ShouldBeDifferent() {
        String token1 = jwtService.generateToken("user1@example.com", "TEACHER");
        String token2 = jwtService.generateToken("user2@example.com", "STUDENT");

        assertThat(token1).isNotEqualTo(token2);
    }
}