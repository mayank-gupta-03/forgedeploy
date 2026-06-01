package com.forgedeploy.service.modules.engine.dto;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.util.UUID;

@Data
@Builder
public class BuildConfiguration {
    private UUID deploymentId;
    private String image;
    private String command;
    private Path workspaceDir;
}
