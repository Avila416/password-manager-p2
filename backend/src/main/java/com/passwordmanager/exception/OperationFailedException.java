package com.passwordmanager.exception;

public class OperationFailedException extends RuntimeException {

    public OperationFailedException(String message) {
        super(message);
    }
}