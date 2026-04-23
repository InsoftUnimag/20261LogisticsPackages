package com.logistics.packages.domain.exception;

public class InvalidCoverageException extends RuntimeException {
    public InvalidCoverageException(String message) {
        super(message);
    }
}
