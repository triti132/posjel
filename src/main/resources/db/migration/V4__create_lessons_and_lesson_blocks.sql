-- V3__create_lessons_and_lesson_blocks.sql

-- Тип блока (Enum в PostgreSQL)
CREATE TYPE block_type_enum AS ENUM ('TEXT', 'CODE', 'TEST');

-- Таблица уроков
CREATE TABLE lessons (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    order_index     INT             NOT NULL DEFAULT 0,
    is_published    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lessons_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX idx_lessons_course_id ON lessons(course_id);
CREATE UNIQUE INDEX idx_lessons_course_order ON lessons(course_id, order_index);

-- Таблица блоков урока
CREATE TABLE lesson_blocks (
    id              BIGSERIAL           PRIMARY KEY,
    lesson_id       BIGINT              NOT NULL,
    block_type      block_type_enum     NOT NULL,
    content         JSONB               NOT NULL,
    order_index     INT                 NOT NULL DEFAULT 0,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lesson_blocks_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);

CREATE INDEX idx_lesson_blocks_lesson_id ON lesson_blocks(lesson_id);
CREATE INDEX idx_lesson_blocks_order ON lesson_blocks(lesson_id, order_index);