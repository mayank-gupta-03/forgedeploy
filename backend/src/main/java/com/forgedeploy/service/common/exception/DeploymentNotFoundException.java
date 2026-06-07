package com.forgedeploy.service.common.exception;

public class DeploymentNotFoundException extends RuntimeException {
    public DeploymentNotFoundException(String message) {
        super(message);
    }
}
