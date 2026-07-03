package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}

