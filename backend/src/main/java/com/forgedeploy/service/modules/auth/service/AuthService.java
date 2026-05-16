package com.forgedeploy.service.modules.auth.service;

import com.forgedeploy.service.common.exception.EmailAlreadyExistsException;
import com.forgedeploy.service.entities.UserInfo;
import com.forgedeploy.service.modules.auth.dto.RegisterUserRequest;
import com.forgedeploy.service.modules.auth.dto.RegisterUserResponse;
import com.forgedeploy.service.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserResponse registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("A user with this email already exists!");
        }

        UserInfo userInfo = UserInfo.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(userInfo);

        return RegisterUserResponse.builder()
                .email(userInfo.getEmail())
                .build();
    }
}
