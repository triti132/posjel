CREATE TYPE course_status AS ENUM ('DRAFT', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE course_visibility AS ENUM ('PUBLIC', 'PRIVATE');

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_url VARCHAR(500),
    discipline VARCHAR(100),
    status course_status NOT NULL DEFAULT 'DRAFT',
    visibility course_visibility NOT NULL DEFAULT 'PUBLIC',
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для ускорения фильтрации каталога
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_author ON courses(author_id);
CREATE INDEX idx_courses_visibility ON courses(visibility);