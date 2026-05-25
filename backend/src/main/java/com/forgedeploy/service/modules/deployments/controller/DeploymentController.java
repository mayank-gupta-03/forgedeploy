package com.forgedeploy.service.modules.deployments.controller;

import com.forgedeploy.service.modules.deployments.dto.CreateDeploymentRequest;
import com.forgedeploy.service.modules.deployments.dto.DeploymentResponse;
import com.forgedeploy.service.modules.deployments.service.DeploymentService;
import com.forgedeploy.service.modules.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deploymentService.createDeployment(request, file, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeploymentResponse> getDeploymentById(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(deploymentService.getDeploymentById(id, principal.getId()));
    }
}
