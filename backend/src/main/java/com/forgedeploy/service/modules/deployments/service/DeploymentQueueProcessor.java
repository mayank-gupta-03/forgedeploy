package com.forgedeploy.service.modules.deployments.service;

import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.DeploymentStatus;
import com.forgedeploy.service.modules.deployments.repository.DeploymentRepository;
import com.forgedeploy.service.modules.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentQueueProcessor {

    private final DeploymentRepository deploymentRepository;
    private final S3Service s3Service;
    private final WorkspaceService workspaceService;

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
            workspaceService.saveToWorkspace(deployment.getId(), file);

            log.info("Mock build finished for deployment: {}", deployment.getId());

            // In Phase 2, this will be replaced with actual build/docker logic
            deployment.setStatus(DeploymentStatus.COMPLETED);
            deploymentRepository.save(deployment);

        } catch (Exception e) {
            log.error("Deployment failed: {}", deployment.getId(), e);
            deployment.setStatus(DeploymentStatus.FAILED);
            deploymentRepository.save(deployment);
        }
    }

    private void extractZip(ZipInputStream inputStream) {

    }
}
