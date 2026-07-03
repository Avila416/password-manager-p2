package com.passwordmanager.dto;


import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BackupRequest {
    private String data;

    public BackupRequest() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

