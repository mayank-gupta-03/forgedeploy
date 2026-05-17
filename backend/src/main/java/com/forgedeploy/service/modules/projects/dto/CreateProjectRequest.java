package com.forgedeploy.service.modules.projects.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;
}
