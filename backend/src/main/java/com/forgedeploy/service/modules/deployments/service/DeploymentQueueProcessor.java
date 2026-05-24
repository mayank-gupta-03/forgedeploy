package com.forgedeploy.service.modules.deployments.service;

import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.DeploymentStatus;
import com.forgedeploy.service.modules.deployments.repository.DeploymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentQueueProcessor {

    private final DeploymentRepository deploymentRepository;

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
            // Mocking the build process for Phase 1
            Thread.sleep(2000); 
            
            log.info("Mock build finished for deployment: {}", deployment.getId());
            
            // In Phase 2, this will be replaced with actual build/docker logic
            deployment.setStatus(DeploymentStatus.COMPLETED);
            deploymentRepository.save(deployment);
            
        } catch (InterruptedException e) {
            log.error("Deployment interrupted: {}", deployment.getId(), e);
            deployment.setStatus(DeploymentStatus.FAILED);
            deploymentRepository.save(deployment);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Deployment failed: {}", deployment.getId(), e);
            deployment.setStatus(DeploymentStatus.FAILED);
            deploymentRepository.save(deployment);
        }
    }
}
