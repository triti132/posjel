CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Уникальный индекс на нижний регистр email (нормализация, как требует ТЗ)
CREATE UNIQUE INDEX idx_users_email_unique ON users(LOWER(email));

-- Индекс для быстрого поиска по роли (например, для админки)
CREATE INDEX idx_users_role ON users(role);