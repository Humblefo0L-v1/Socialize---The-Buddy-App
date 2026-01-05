package com.socialize.request.exception;

public class IneligibleRequestException extends RuntimeException {
    public IneligibleRequestException(String message) {
        super(message);
    }
}