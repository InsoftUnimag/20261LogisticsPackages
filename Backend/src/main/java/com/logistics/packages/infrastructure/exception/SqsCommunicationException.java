package com.logistics.packages.infrastructure.exception;

public class SqsCommunicationException extends RuntimeException {

    public SqsCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqsCommunicationException(String message) {
        super(message);
    }
}
