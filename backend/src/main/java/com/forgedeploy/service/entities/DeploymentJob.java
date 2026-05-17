package com.forgedeploy.service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "deployment_jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeploymentJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id", nullable = false)
    private Deployment deployment;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "error_message")
    private String errorMessage;
}
