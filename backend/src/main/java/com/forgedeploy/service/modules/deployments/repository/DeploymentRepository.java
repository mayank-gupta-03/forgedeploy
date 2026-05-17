package com.forgedeploy.service.modules.deployments.repository;

import com.forgedeploy.service.entities.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {
}
