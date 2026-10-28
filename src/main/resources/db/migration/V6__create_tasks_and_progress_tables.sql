-- Создание ENUM типов для заданий и попыток
CREATE TYPE task_type_enum AS ENUM ('SINGLE_CHOICE_TEST', 'SHORT_TEXT');
CREATE TYPE attempt_status_enum AS ENUM ('CORRECT', 'INCORRECT');

-- Таблица заданий
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    task_type task_type_enum NOT NULL,
    condition_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL, -- Хранится только на бэкенде
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица попыток выполнения
CREATE TABLE task_attempts (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    student_answer TEXT NOT NULL,
    status attempt_status_enum NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица прогресса урока
CREATE TABLE lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    CONSTRAINT unique_user_lesson_progress UNIQUE (lesson_id, user_id)
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_tasks_lesson ON tasks(lesson_id);
CREATE INDEX idx_task_attempts_task_user ON task_attempts(task_id, user_id);
CREATE INDEX idx_lesson_progress_user ON lesson_progress(user_id);
CREATE INDEX idx_lesson_progress_lesson ON lesson_progress(lesson_id);