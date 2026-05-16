package com.forgedeploy.service.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserResponse {
    private String email;
}