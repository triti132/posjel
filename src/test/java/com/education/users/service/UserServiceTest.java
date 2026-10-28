package com.education.users.service;

import com.education.common.exception.BusinessException;
import com.education.users.dto.request.RegisterRequest;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("test@example.com", "password123", "Иван Иванов");
    }

    @Test
    void register_Success_ReturnsUserResponse() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        var response = userService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals(Role.STUDENT, response.role());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsBusinessException() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(validRequest);
        });

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}