package com.forgedeploy.service.modules.deployments.dto;

import com.forgedeploy.service.entities.DeploymentStatus;
import com.forgedeploy.service.entities.SourceType;
import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentResponse {
    private UUID id;
    private UUID projectId;
    private SourceType sourceType;
    private ProjectType projectType;
    private String repoUrl;
    private DeploymentStatus status;
    private LocalDateTime createdAt;
}
