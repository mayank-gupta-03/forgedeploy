package com.forgedeploy.service.modules.deployments.dto;

import com.forgedeploy.service.entities.SourceType;
import com.forgedeploy.service.modules.engine.dto.enums.ProjectType;
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

    @NotNull(message = "Source type is required")
    private SourceType sourceType;

    @NotNull(message = "Project type is required")
    private ProjectType projectType;

    private String repoUrl;

    private String buildCommand;

    private String outputDirectory;
}
