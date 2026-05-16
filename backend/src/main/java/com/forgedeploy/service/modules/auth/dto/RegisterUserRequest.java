package com.forgedeploy.service.modules.auth.dto;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String email;
    private String password;
}
