package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class AuditException extends RuntimeException {
    public AuditException(String msg) {
        super(msg);
    }
}
