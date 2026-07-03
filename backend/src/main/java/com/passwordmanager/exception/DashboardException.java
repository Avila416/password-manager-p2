package com.passwordmanager.exception;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class DashboardException extends RuntimeException {
    public DashboardException(String msg) {
        super(msg);
    }
}
