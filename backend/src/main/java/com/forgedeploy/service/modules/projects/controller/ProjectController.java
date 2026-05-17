package com.forgedeploy.service.modules.projects.controller;

import com.forgedeploy.service.modules.projects.dto.CreateProjectRequest;
import com.forgedeploy.service.modules.projects.dto.ProjectResponse;
import com.forgedeploy.service.modules.projects.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(Principal principal) {
        return ResponseEntity.ok(projectService.getProjects(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(projectService.getProjectById(id, principal.getName()));
    }
}
