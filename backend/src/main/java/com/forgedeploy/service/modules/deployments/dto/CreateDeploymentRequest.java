package com.forgedeploy.service.modules.deployments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeploymentRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Source type is required")
    private String sourceType; // 'GITHUB' or 'ZIP'

    private String repoUrl;

    private String buildCommand;

    private String outputDirectory;
}
