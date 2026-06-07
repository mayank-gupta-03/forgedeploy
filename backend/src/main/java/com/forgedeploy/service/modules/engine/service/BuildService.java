package com.forgedeploy.service.modules.engine.service;

import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.modules.engine.dto.BuildConfiguration;
import com.forgedeploy.service.modules.engine.strategy.BuildStrategy;
import com.forgedeploy.service.modules.engine.strategy.BuildStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class BuildService {
    private final WorkspaceService workspaceService;
    private final ContainerService containerService;
    private final BuildStrategyFactory strategyFactory;

    public void orchestrateBuild(Deployment deployment) {
        BuildStrategy strategy = strategyFactory.getStrategy(deployment.getProjectType());
        Path workspaceDir = workspaceService.getWorkspacePath(deployment.getId());
        String buildCommand = (deployment.getBuildCommand() == null || deployment.getBuildCommand().isBlank())
                ? strategy.getDefaultBuildCommand()
                : deployment.getBuildCommand();
        BuildConfiguration buildConfig = mapToBuildConfiguration(deployment, workspaceDir, strategy.getDefaultImage(), buildCommand);
        containerService.executeBuild(buildConfig);
    }

    public BuildConfiguration mapToBuildConfiguration(Deployment deployment, Path workspaceDir, String image, String buildCommand) {
        return BuildConfiguration.builder()
                .deploymentId(deployment.getId())
                .workspaceDir(workspaceDir)
                .command(buildCommand)
                .image(image)
                .build();
    }
}
