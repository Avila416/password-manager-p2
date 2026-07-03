package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BackupRequiredException extends RuntimeException {
    public BackupRequiredException(String msg) {
        super(msg);
    }
}

