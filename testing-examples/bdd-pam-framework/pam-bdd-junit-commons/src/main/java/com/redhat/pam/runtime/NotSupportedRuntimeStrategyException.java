package com.redhat.pam.runtime;

public class NotSupportedRuntimeStrategyException extends RuntimeException {
    
    private static final long serialVersionUID = 7110275990624882801L;

    public NotSupportedRuntimeStrategyException(String message) {
        super(message);
    }
}
