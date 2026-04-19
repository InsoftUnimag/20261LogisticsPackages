package com.logistics.packages.domain.exception;

public class TimeoutGeocodingException extends RuntimeException {
    public TimeoutGeocodingException(String message) {
        super(message);
    }
}
