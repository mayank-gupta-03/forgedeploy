package com.forgedeploy.service.modules.deployments.service;

import com.forgedeploy.service.common.exception.DeploymentNotFoundException;
import com.forgedeploy.service.common.exception.ProjectNotFoundException;
import com.forgedeploy.service.common.exception.StorageException;
import com.forgedeploy.service.entities.SourceType;
import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.DeploymentStatus;
import com.forgedeploy.service.entities.Project;
import com.forgedeploy.service.modules.deployments.dto.CreateDeploymentRequest;
import com.forgedeploy.service.modules.deployments.dto.DeploymentResponse;
import com.forgedeploy.service.modules.deployments.repository.DeploymentRepository;
import com.forgedeploy.service.modules.projects.repository.ProjectRepository;
import com.forgedeploy.service.modules.s3.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeploymentService {
    private final DeploymentRepository deploymentRepository;
    private final ProjectRepository projectRepository;
    private final S3Service s3Service;

    @Transactional
    public DeploymentResponse createDeployment(CreateDeploymentRequest request, MultipartFile file, UUID userId) {
        Project project = projectRepository.findById(request.getProjectId())
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new ProjectNotFoundException("Project not found or access denied"));

        Deployment deployment = Deployment.builder()
                .project(project)
                .sourceType(request.getSourceType())
                .projectType(request.getProjectType())
                .repoUrl(request.getRepoUrl())
                .buildCommand(request.getBuildCommand() != null ? request.getBuildCommand() : null)
                .outputDirectory(request.getOutputDirectory() != null ? request.getOutputDirectory() : null)
                .status(DeploymentStatus.QUEUED)
                .build();

        deploymentRepository.save(deployment);

        String key = "projects/" + project.getId() + "/deployments/" + deployment.getId() + "/source.zip";

        if (SourceType.ZIP.equals(request.getSourceType())) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("ZIP file is required when source type is ZIP");
            }
            deployment.setRepoUrl("local-zip");

            try {
                uploadZipToS3(file, key);
                deployment.setStorageKey(key);
            } catch (IOException e) {
                deployment.setStatus(DeploymentStatus.FAILED);
                deployment.setErrorMessage("S3 Upload failed: " + e.getMessage());
                log.error("Deployment storage failed for project: {}, deployment: {}", project.getId(), deployment.getId(), e);
                throw new StorageException("Failed to upload source ZIP to storage", e);
            }
        }

        deployment = deploymentRepository.save(deployment);
        log.info("Successfully created deployment: {} for project: {}", deployment.getId(), project.getId());
        return mapToResponse(deployment);
    }

    public DeploymentResponse getDeploymentById(UUID id, UUID userId) {
        Deployment deployment = deploymentRepository.findById(id)
                .filter(d -> d.getProject().getUser().getId().equals(userId))
                .orElseThrow(() -> new DeploymentNotFoundException("Deployment not found or access denied"));

        return mapToResponse(deployment);
    }

    private void uploadZipToS3(MultipartFile file, String key) throws IOException {
        s3Service.uploadInputStream(key, file.getInputStream(), file.getSize(), file.getContentType());
    }

    private DeploymentResponse mapToResponse(Deployment deployment) {
        return DeploymentResponse.builder()
                .id(deployment.getId())
                .projectId(deployment.getProject().getId())
                .sourceType(deployment.getSourceType())
                .projectType(deployment.getProjectType())
                .repoUrl(deployment.getRepoUrl())
                .status(deployment.getStatus())
                .createdAt(deployment.getCreatedAt())
                .build();
    }
}
