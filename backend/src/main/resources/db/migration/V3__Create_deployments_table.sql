CREATE TABLE deployments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id),
    source_type VARCHAR(50) NOT NULL,
    repo_url VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255),
    build_command VARCHAR(255),
    output_directory VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    log_path VARCHAR(255),
    build_duration BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE deployment_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deployment_id UUID NOT NULL REFERENCES deployments(id),
    status VARCHAR(50) NOT NULL,
    attempts INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
