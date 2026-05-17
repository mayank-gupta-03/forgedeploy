package com.forgedeploy.service.modules.deployments.controller;

import com.forgedeploy.service.modules.deployments.dto.CreateDeploymentRequest;
import com.forgedeploy.service.modules.deployments.dto.DeploymentResponse;
import com.forgedeploy.service.modules.deployments.service.DeploymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deployments")
@RequiredArgsConstructor
public class DeploymentController {
    private final DeploymentService deploymentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeploymentResponse> createDeployment(
            @Valid @RequestPart("request") CreateDeploymentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deploymentService.createDeployment(request, file, principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeploymentResponse> getDeploymentById(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(deploymentService.getDeploymentById(id, principal.getName()));
    }
}
