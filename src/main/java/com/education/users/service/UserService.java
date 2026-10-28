package com.education.users.service;

import com.education.common.exception.BusinessException;
import com.education.users.dto.request.RegisterRequest;
import com.education.users.dto.response.UserResponse;
import com.education.users.entity.Role;
import com.education.users.entity.User;
import com.education.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Проверка уникальности email
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Пользователь с таким email уже зарегистрирован");
        }

        // 2. Создание пользователя (ВНИМАНИЕ: строка .password() должна быть обязательно!)
        User user = User.builder()
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password())) // <-- ВОТ ЭТА СТРОКА ХЕШИРУЕТ И ПРИСВАИВАЕТ ПАРОЛЬ
                .fullName(request.fullName())
                .role(Role.STUDENT)
                .build();

        User savedUser = userRepository.save(user);

        // 3. Возврат DTO
        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());
    }
}