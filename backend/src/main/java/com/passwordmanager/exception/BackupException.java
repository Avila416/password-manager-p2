package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BackupException extends RuntimeException {
    public BackupException(String msg) {
        super(msg);
    }
}
