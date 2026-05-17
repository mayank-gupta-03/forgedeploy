package com.forgedeploy.service.modules.deployments.service;

import com.forgedeploy.service.common.exception.ProjectNotFoundException;
import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.Project;
import com.forgedeploy.service.modules.deployments.dto.CreateDeploymentRequest;
import com.forgedeploy.service.modules.deployments.dto.DeploymentResponse;
import com.forgedeploy.service.modules.deployments.repository.DeploymentRepository;
import com.forgedeploy.service.modules.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentService {
    private final DeploymentRepository deploymentRepository;
    private final ProjectRepository projectRepository;

    private static final String UPLOADS_DIR = "uploads";

    public DeploymentResponse createDeployment(CreateDeploymentRequest request, MultipartFile file, UUID userId) {
        Project project = projectRepository.findById(request.getProjectId())
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new ProjectNotFoundException("Project not found or access denied"));

        Deployment deployment = Deployment.builder()
                .project(project)
                .sourceType(request.getSourceType())
                .repoUrl(request.getRepoUrl())
                .buildCommand(request.getBuildCommand() != null ? request.getBuildCommand() : "npm run build")
                .outputDirectory(request.getOutputDirectory() != null ? request.getOutputDirectory() : "dist")
                .status("QUEUED")
                .build();

        if ("ZIP".equalsIgnoreCase(request.getSourceType())) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("ZIP file is required when source type is ZIP");
            }
            deployment.setRepoUrl("local-zip");
        }

        deployment = deploymentRepository.save(deployment);

        if ("ZIP".equalsIgnoreCase(request.getSourceType())) {
            saveZipFile(deployment.getId(), file);
        }

        return mapToResponse(deployment);
    }

    public DeploymentResponse getDeploymentById(UUID id, UUID userId) {
        Deployment deployment = deploymentRepository.findById(id)
                .filter(d -> d.getProject().getUser().getId().equals(userId))
                .orElseThrow(() -> new ProjectNotFoundException("Deployment not found or access denied"));

        return mapToResponse(deployment);
    }

    private void saveZipFile(UUID deploymentId, MultipartFile file) {
        try {
            Path root = Paths.get(UPLOADS_DIR);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            Files.copy(file.getInputStream(), root.resolve(deploymentId + ".zip"));
        } catch (IOException e) {
            log.error("Could not save ZIP file for deployment: {}", deploymentId, e);
            throw new RuntimeException("Failed to store deployment file", e);
        }
    }

    private DeploymentResponse mapToResponse(Deployment deployment) {
        return DeploymentResponse.builder()
                .id(deployment.getId())
                .projectId(deployment.getProject().getId())
                .sourceType(deployment.getSourceType())
                .repoUrl(deployment.getRepoUrl())
                .status(deployment.getStatus())
                .createdAt(deployment.getCreatedAt())
                .build();
    }
}
