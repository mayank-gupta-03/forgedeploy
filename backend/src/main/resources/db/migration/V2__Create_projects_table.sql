CREATE TABLE projects
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users (id),
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
