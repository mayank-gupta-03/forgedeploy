package com.forgedeploy.service.modules.projects.service;

import com.forgedeploy.service.common.exception.ProjectNotFoundException;
import com.forgedeploy.service.entities.Project;
import com.forgedeploy.service.entities.UserInfo;
import com.forgedeploy.service.modules.projects.dto.CreateProjectRequest;
import com.forgedeploy.service.modules.projects.dto.ProjectResponse;
import com.forgedeploy.service.modules.projects.repository.ProjectRepository;
import com.forgedeploy.service.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        Project project = Project.builder()
                .name(request.getName())
                .user(user)
                .build();

        project = projectRepository.save(project);

        return mapToResponse(project);
    }

    public List<ProjectResponse> getProjects(UUID userId) {
        return projectRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(UUID id, UUID userId) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new ProjectNotFoundException("Project not found or access denied"));

        return mapToResponse(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
