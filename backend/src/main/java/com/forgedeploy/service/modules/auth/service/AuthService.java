package com.forgedeploy.service.modules.auth.service;

import com.forgedeploy.service.common.exception.EmailAlreadyExistsException;
import com.forgedeploy.service.entities.UserInfo;
import com.forgedeploy.service.modules.auth.dto.LoginRequest;
import com.forgedeploy.service.modules.auth.dto.LoginResponse;
import com.forgedeploy.service.modules.auth.dto.RegisterUserRequest;
import com.forgedeploy.service.modules.auth.dto.RegisterUserResponse;
import com.forgedeploy.service.modules.security.jwt.JwtService;
import com.forgedeploy.service.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserInfo user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtService.generateToken(request.getEmail(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .email(request.getEmail())
                .build();
    }
}
