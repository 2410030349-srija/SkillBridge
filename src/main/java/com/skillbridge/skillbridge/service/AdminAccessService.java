package com.skillbridge.skillbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAccessService {

    private final String adminKey;

    public AdminAccessService(@Value("${app.security.admin-key}") String adminKey) {
        this.adminKey = adminKey;
    }

    public void validateAdminKey(String providedKey) {
        if (providedKey == null || providedKey.isBlank() || !adminKey.equals(providedKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }
    }
}