package com.passwordmanager.util;

import org.springframework.stereotype.Component;

@Component
public class FileUtil {

    public boolean validate(String content) {
        return content != null && !content.trim().isEmpty();
    }
}
