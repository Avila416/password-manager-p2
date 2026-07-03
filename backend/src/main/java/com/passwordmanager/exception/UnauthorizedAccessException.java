package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
