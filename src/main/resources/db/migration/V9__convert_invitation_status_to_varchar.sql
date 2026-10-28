-- Конвертируем колонку status из кастомного enum invitation_status в VARCHAR
-- Это соответствует подходу, использованному в миграциях V3 и V7

-- 1. Добавляем временную VARCHAR-колонку
ALTER TABLE course_invitations ADD COLUMN status_temp VARCHAR(50);

-- 2. Копируем данные из enum-колонки в VARCHAR (PostgreSQL автоматически конвертирует enum в текст)
UPDATE course_invitations SET status_temp = status::text;

-- 3. Удаляем старую колонку
ALTER TABLE course_invitations DROP COLUMN status;

-- 4. Переименовываем временную колонку
ALTER TABLE course_invitations RENAME COLUMN status_temp TO status;

-- 5. Делаем колонку NOT NULL с дефолтным значением
ALTER TABLE course_invitations ALTER COLUMN status SET NOT NULL;
ALTER TABLE course_invitations ALTER COLUMN status SET DEFAULT 'PENDING';

-- 6. Удаляем неиспользуемый enum-тип
DROP TYPE IF EXISTS invitation_status;