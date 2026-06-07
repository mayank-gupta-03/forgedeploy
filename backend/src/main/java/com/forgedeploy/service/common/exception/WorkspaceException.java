package com.forgedeploy.service.common.exception;

public class WorkspaceException extends RuntimeException {
    public WorkspaceException(String message) {
        super(message);
    }

    public WorkspaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
