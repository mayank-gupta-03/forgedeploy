package com.forgedeploy.service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deployments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Deployment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "repo_url", nullable = false)
    private String repoUrl;

    @Column(name = "build_command")
    private String buildCommand;

    @Column(name = "output_directory")
    private String outputDirectory;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
