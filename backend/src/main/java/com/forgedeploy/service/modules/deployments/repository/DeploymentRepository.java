package com.forgedeploy.service.modules.deployments.repository;

import com.forgedeploy.service.entities.Deployment;
import com.forgedeploy.service.entities.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {
    List<Deployment> findByStatusOrderByCreatedAtAsc(DeploymentStatus status);
}
