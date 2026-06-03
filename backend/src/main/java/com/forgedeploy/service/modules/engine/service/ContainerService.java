package com.forgedeploy.service.modules.engine.service;

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
            dockerClient.startContainerCmd(containerId).exec();
            waitForCompletion(containerId, config.getDeploymentId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Build process was interrupted for deployment {}", config.getDeploymentId(), e);
            throw new RuntimeException("Build interrupted", e);
        } finally {
            if (containerId != null) cleanupContainer(containerId);
        }
    }

    private void pullImage(String imageName) throws InterruptedException {
        log.info("Pulling image: {}", imageName);

        String[] imageParts = imageName.split(":");
        String repository = imageParts[0];
        String tag = imageParts[1];

        dockerClient.pullImageCmd(repository).withTag(tag).start().awaitCompletion();
        log.info("Successfully pulled image: {}", imageName);
    }

    private String createContainer(BuildConfiguration config) {
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
    }

    private void waitForCompletion(String containerId, UUID deploymentId) {
        log.info("Waiting for build execution to complete...");

        Integer exitCode = dockerClient.waitContainerCmd(containerId)
                .start()
                .awaitStatusCode();

        if (exitCode == 0) {
            log.info("Build finished successfully for deployment {}", deploymentId);
        } else {
            log.error("Build failed for deployment {} with exit code {}", deploymentId, exitCode);
            dockerClient.logContainerCmd(containerId);
            throw new RuntimeException("Docker build failed with exit code: " + exitCode);
        }
    }

    private void cleanupContainer(String containerId) {
        log.info("Cleaning up build container: {}", containerId);
        dockerClient.removeContainerCmd(containerId)
                .withForce(true)
                .exec();
    }
}
