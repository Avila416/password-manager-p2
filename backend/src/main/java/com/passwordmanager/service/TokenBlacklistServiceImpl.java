package com.passwordmanager.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.passwordmanager.security.JwtUtil;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    public TokenBlacklistServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void blacklistToken(String token) {
        Date expiry = jwtUtil.extractExpiration(token);
        blacklist.put(token, expiry.getTime());
    }

    @Override
    public boolean isBlacklisted(String token) {
        Long expiry = blacklist.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
