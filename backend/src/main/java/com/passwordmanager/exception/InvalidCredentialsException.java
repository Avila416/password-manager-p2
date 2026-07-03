package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
