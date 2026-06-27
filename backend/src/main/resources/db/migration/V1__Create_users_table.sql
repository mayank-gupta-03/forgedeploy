CREATE TABLE users
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255),
    github_id           VARCHAR(255),
    github_username     VARCHAR(255),
    github_access_token VARCHAR(255),
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
