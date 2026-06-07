package com.forgedeploy.service.modules.engine.service;

import com.forgedeploy.service.common.exception.WorkspaceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public Path saveToWorkspace(UUID deploymentId, InputStream inputStream) {
        Path workspace = createWorkspace(deploymentId);
        Path targetFilePath = workspace.resolve("source.zip");

        log.info("Saving source ZIP to workspace: {}", targetFilePath);
        try {
            Files.copy(inputStream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            return targetFilePath;
        } catch (IOException e) {
            log.error("Failed to save input stream to workspace for deployment: {}", deploymentId, e);
            throw new WorkspaceException("Failed to store file in workspace", e);
        }
    }

    public Path getWorkspacePath(UUID deploymentId) {
        return Paths.get(BASE_PATH, deploymentId.toString());
    }

    private Path createWorkspace(UUID deploymentId) {
        Path workspacePath = getWorkspacePath(deploymentId);
        log.info("Creating workspace directory: {}", workspacePath);
        try {
            if (Files.exists(workspacePath)) {
                log.debug("Workspace already exists, clearing it: {}", workspacePath);
                deleteDirectory(workspacePath);
            }
            Files.createDirectories(workspacePath);
            return workspacePath;
        } catch (IOException e) {
            log.error("Failed to create directories for deployment: {}", deploymentId, e);
            throw new WorkspaceException("Could not initialize workspace storage", e);
        }
    }

    private void deleteDirectory(Path pathToBeDeleted) {
        log.debug("Deleting directory: {}", pathToBeDeleted);
        try (Stream<Path> stream = Files.walk(pathToBeDeleted)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Failed to delete path: {}", path, e);
                    throw new WorkspaceException("Failed to clear workspace path: " + path, e);
                }
            });
        } catch (IOException e) {
            log.error("Failed to walk directory for deletion: {}", pathToBeDeleted, e);
            throw new WorkspaceException("Failed to clear workspace", e);
        }
    }
}
