package com.forgedeploy.service.modules.projects.controller;

import com.forgedeploy.service.modules.projects.dto.CreateProjectRequest;
import com.forgedeploy.service.modules.projects.dto.ProjectResponse;
import com.forgedeploy.service.modules.projects.service.ProjectService;
import com.forgedeploy.service.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjects(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjectById(id, principal.getId()));
    }
}
