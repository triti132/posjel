-- Изменяем типы столбцов с пользовательских ENUM на VARCHAR.
-- Это стандартная практика для Hibernate + PostgreSQL,
-- которая предотвращает ошибки приведения типов при @Enumerated(EnumType.STRING)

ALTER TABLE courses ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
ALTER TABLE courses ALTER COLUMN visibility TYPE VARCHAR(50) USING visibility::text;

-- Обновляем значения по умолчанию на текстовые строки
ALTER TABLE courses ALTER COLUMN status SET DEFAULT 'DRAFT';
ALTER TABLE courses ALTER COLUMN visibility SET DEFAULT 'PUBLIC';