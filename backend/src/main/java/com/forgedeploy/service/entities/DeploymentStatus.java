package com.forgedeploy.service.entities;

public enum DeploymentStatus {
    QUEUED,
    CLONING,
    BUILDING,
    UPLOADING,
    COMPLETED,
    FAILED
}
