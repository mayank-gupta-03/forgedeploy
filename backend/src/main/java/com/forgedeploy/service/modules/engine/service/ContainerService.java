package com.forgedeploy.service.modules.engine.service;

import com.forgedeploy.service.common.exception.BuildFailedException;
import com.forgedeploy.service.modules.engine.dto.BuildConfiguration;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerService {
    private final DockerClient dockerClient;

    public void executeBuild(BuildConfiguration config) {
        String containerId = null;

        try {
            pullImage(config.getImage());
            containerId = createContainer(config);
            log.info("Starting container {} for deployment {}", containerId, config.getDeploymentId());
            dockerClient.startContainerCmd(containerId).exec();
            waitForCompletion(containerId, config.getDeploymentId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Build process was interrupted for deployment {}", config.getDeploymentId(), e);
            throw new BuildFailedException("Build process was interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error during build execution for deployment {}", config.getDeploymentId(), e);
            throw new BuildFailedException("Docker build failed: " + e.getMessage(), e);
        } finally {
            if (containerId != null) cleanupContainer(containerId);
        }
    }

    private void pullImage(String imageName) throws InterruptedException {
        try {
            log.info("Pulling image: {}", imageName);

            String[] imageParts = imageName.split(":");
            String repository = imageParts[0];
            String tag = (imageParts.length > 1 && !imageParts[1].isBlank()) ? imageParts[1] : "latest";

            dockerClient.pullImageCmd(repository).withTag(tag).start().awaitCompletion();
            log.info("Successfully pulled image: {}", imageName);
        } catch (Exception e) {
            log.error("Failed to pull image: {}", imageName, e);
            throw new BuildFailedException("Failed to pull required build image: " + imageName, e);
        }
    }

    private String createContainer(BuildConfiguration config) {
        try {
            Volume containerWorkspace = new Volume("/app");

            Bind workspaceBind = new Bind(config.getWorkspaceDir().toAbsolutePath().toString(), containerWorkspace);
            HostConfig hostConfig = HostConfig.newHostConfig().withBinds(workspaceBind);

            CreateContainerResponse container = dockerClient.createContainerCmd(config.getImage())
                    .withName("build-worker-" + config.getDeploymentId())
                    .withWorkingDir("/app")
                    .withHostConfig(hostConfig)
                    .withCmd("sh", "-c", config.getCommand())
                    .exec();

            return container.getId();
        } catch (Exception e) {
            log.error("Failed to create container for deployment {}", config.getDeploymentId(), e);
            throw new BuildFailedException("Failed to create build container", e);
        }
    }

    private void waitForCompletion(String containerId, UUID deploymentId) {
        log.info("Waiting for build execution to complete for container {}", containerId);

        Integer exitCode = dockerClient.waitContainerCmd(containerId)
                .start()
                .awaitStatusCode();

        if (exitCode == 0) {
            log.info("Build finished successfully for deployment {}", deploymentId);
        } else {
            log.error("Build failed for deployment {} with exit code {}", deploymentId, exitCode);
            throw new BuildFailedException("Docker build failed with exit code: " + exitCode);
        }
    }

    private void cleanupContainer(String containerId) {
        log.info("Cleaning up build container: {}", containerId);
        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
    }
}
