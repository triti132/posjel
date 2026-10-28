-- Приводим типы enum к varchar, чтобы Hibernate мог работать без кастов
ALTER TABLE tasks
    ALTER COLUMN task_type TYPE VARCHAR(50) USING task_type::text;

ALTER TABLE task_attempts
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- Удаляем ненужные enum-типы
DROP TYPE IF EXISTS task_type_enum;
DROP TYPE IF EXISTS attempt_status_enum;