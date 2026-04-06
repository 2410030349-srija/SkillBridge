package com.skillbridge.skillbridge.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContentView(
        Long id,
        String title,
        String description,
        String domain,
        List<String> tags,
        String resourceLink,
        String resourceFile,
        String uploadedBy,
        boolean verified,
        long views,
        long likes,
        long dislikes,
        double averageRating,
        LocalDateTime createdAt
) {
}
