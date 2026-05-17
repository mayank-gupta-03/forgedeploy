package com.forgedeploy.service.modules.deployments.dto;

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
    private String sourceType;
    private String repoUrl;
    private String status;
    private LocalDateTime createdAt;
}
