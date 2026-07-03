package com.passwordmanager.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FileUtil {

    public boolean validate(String content) {
        return content != null && !content.trim().isEmpty();
    }
}

