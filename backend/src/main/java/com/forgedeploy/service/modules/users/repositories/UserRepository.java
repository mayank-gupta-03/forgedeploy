package com.forgedeploy.service.modules.users.repositories;

import com.forgedeploy.service.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserInfo, UUID> {
    Optional<UserInfo> findByEmail(String email);
    boolean existsByEmail(String email);
}
