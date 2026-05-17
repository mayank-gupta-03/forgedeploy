package com.forgedeploy.service.modules.deployments.repository;

import com.forgedeploy.service.entities.DeploymentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeploymentJobRepository extends JpaRepository<DeploymentJob, UUID> {
}
