CREATE TABLE stored_files (
    id                BIGSERIAL       PRIMARY KEY,
    original_filename VARCHAR(255)    NOT NULL,
    stored_filename   VARCHAR(255)    NOT NULL UNIQUE,
    mime_type         VARCHAR(100)    NOT NULL,
    size_bytes        BIGINT          NOT NULL,
    course_id         BIGINT,
    uploaded_by       BIGINT          NOT NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_stored_files_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    CONSTRAINT fk_stored_files_user
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_stored_files_course_id ON stored_files(course_id);
CREATE INDEX idx_stored_files_uploaded_by ON stored_files(uploaded_by);