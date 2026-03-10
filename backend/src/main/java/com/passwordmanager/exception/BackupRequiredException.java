package com.passwordmanager.exception;

public class BackupRequiredException extends RuntimeException {
    public BackupRequiredException(String msg) {
        super(msg);
    }
}
