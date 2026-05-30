package com.forgedeploy.service.modules.deployments.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
public class WorkspaceService {
    private static final String BASE_PATH = "/tmp/forgedeploy/workspaces/";

    public void saveToWorkspace(UUID deploymentId, InputStream inputStream) {
        Path workspace = createWorkspace(deploymentId);
        Path targetFilePath = workspace.resolve("source.zip");

        try {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save input stream to workspace for deployment: {}", deploymentId, e);
            throw new RuntimeException("Failed to store file in workspace", e);
        }
    }

    private Path createWorkspace(UUID deploymentId) {
        Path workspacePath = Paths.get(BASE_PATH, deploymentId.toString());
        try {
            if (Files.exists(workspacePath)) {
                deleteDirectory(workspacePath);
            }
            Files.createDirectories(workspacePath);
            return workspacePath;
        } catch (IOException e) {
            log.error("Failed to create directories for deployment: {}", deploymentId, e);
            throw new RuntimeException("Could not initialize workspace storage", e);
        }
    }

    private void deleteDirectory(Path pathToBeDeleted) {
        try (Stream<Path> stream = Files.walk(pathToBeDeleted)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear workspace", e);
        }
    }
}
