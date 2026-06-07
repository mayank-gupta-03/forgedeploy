package com.forgedeploy.service.modules.deployments.service;

import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.DeploymentStatus;
import com.forgedeploy.service.modules.deployments.repository.DeploymentRepository;
import com.forgedeploy.service.modules.engine.service.BuildService;
import com.forgedeploy.service.modules.engine.service.WorkspaceService;
import com.forgedeploy.service.modules.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentQueueProcessor {

    private final DeploymentRepository deploymentRepository;
    private final S3Service s3Service;
    private final WorkspaceService workspaceService;
    private final BuildService buildService;

    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        log.trace("Polling for queued deployments...");
        List<Deployment> queuedDeployments = deploymentRepository.findByStatusOrderByCreatedAtAsc(DeploymentStatus.QUEUED);

        for (Deployment deployment : queuedDeployments) {
            log.info("Found queued deployment: {}. Starting processing...", deployment.getId());
            startDeployment(deployment);
        }
    }

    @Transactional
    public void startDeployment(Deployment deployment) {
        // Simple "locking" mechanism: move to CLONING status
        deployment.setStatus(DeploymentStatus.CLONING);
        deploymentRepository.save(deployment);

        // Pass to async worker
        processDeploymentAsync(deployment);
    }

    @Async("deploymentTaskExecutor")
    public void processDeploymentAsync(Deployment deployment) {
        log.info("Async worker started for deployment: {}", deployment.getId());

        try {
            String key = deployment.getStorageKey();
            InputStream file = s3Service.downloadFile(key);
            Path zipFilePath = workspaceService.saveToWorkspace(deployment.getId(), file);
            extractZip(zipFilePath);

            // Update status to BUILDING
            deployment.setStatus(DeploymentStatus.BUILDING);
            deploymentRepository.save(deployment);

            log.info("Starting Docker build for deployment: {}", deployment.getId());
            buildService.orchestrateBuild(deployment);

            log.info("Build finished successfully for deployment: {}", deployment.getId());

            // Transition to COMPLETED (later this will be UPLOADING_ARTIFACTS)
            deployment.setStatus(DeploymentStatus.COMPLETED);
            deployment.setErrorMessage(null); // Clear any previous error
            deploymentRepository.save(deployment);

        } catch (Exception e) {
            log.error("Deployment failed: {}", deployment.getId(), e);
            deployment.setStatus(DeploymentStatus.FAILED);
            deployment.setErrorMessage(e.getMessage());
            deploymentRepository.save(deployment);
        }
    }

    private void extractZip(Path zipFilePath) throws IOException {
        Path targetDir = zipFilePath.getParent();

        if (targetDir == null) {
            throw new IOException("Invalid ZIP file destination workspace.");
        }

        cleanDirectoryExceptZip(targetDir, zipFilePath);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName()).normalize();

                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Bad zip entry" + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void cleanDirectoryExceptZip(Path targetDir, Path zipFilePath) {
        try (Stream<Path> paths = Files.walk(targetDir)) {
            List<Path> pathsToDelete = paths
                    .sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(targetDir))
                    .filter(path -> !path.equals(zipFilePath))
                    .toList();

            for (Path path : pathsToDelete) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.error("Failed to clean the workspace for file path: {}", zipFilePath, e);
            throw new RuntimeException("Unable to clean workspace", e);
        }
    }
}
