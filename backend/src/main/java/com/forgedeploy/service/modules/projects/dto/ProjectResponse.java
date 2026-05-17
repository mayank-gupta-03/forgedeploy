package com.forgedeploy.service.modules.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
}
