package com.skillbridge.skillbridge.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.exception.ModerationViolationException;

@Service
public class ContentModerationService {

    private static final Pattern SAFE_SKILL_PATTERN = Pattern.compile("^[a-zA-Z0-9+#.\\-\\s]{1,50}$");
    private static final Pattern SAFE_URL_PATTERN = Pattern.compile("^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}([/?#].*)?$");
    private static final List<String> BLOCKED_TERMS = List.of(
            "porn", "nude", "sexual", "escort", "weapon", "bomb", "kill", "suicide",
            "drug", "drugs", "casino", "gambling", "torrent", "piracy"
    );

    private final ModerationEventService moderationEventService;

    public ContentModerationService(ModerationEventService moderationEventService) {
        this.moderationEventService = moderationEventService;
    }

    public void ensureSafeText(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (containsBlockedContent(normalized)) {
            moderationEventService.recordBlockedEvent(fieldName, value, "blocked-term");
            throw new ModerationViolationException(fieldName + " contains blocked content");
        }

        if (containsLink(normalized)) {
            moderationEventService.recordBlockedEvent(fieldName, value, "link-detected");
            throw new ModerationViolationException(fieldName + " contains blocked content");
        }
    }

    public void ensureSafeOptionalText(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        ensureSafeText(fieldName, value);
    }

    public void ensureSafeOptionalUrl(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (containsBlockedContent(normalized)) {
            moderationEventService.recordBlockedEvent(fieldName, value, "blocked-term");
            throw new ModerationViolationException(fieldName + " contains blocked content");
        }

        if (!SAFE_URL_PATTERN.matcher(value.trim()).matches()) {
            moderationEventService.recordBlockedEvent(fieldName, value, "invalid-url");
            throw new ModerationViolationException(fieldName + " must be a valid URL");
        }
    }

    public String normalizeSkill(String skill) {
        ensureSafeText("skill", skill);

        String normalized = skill.trim().toLowerCase(Locale.ROOT);
        if (!SAFE_SKILL_PATTERN.matcher(normalized).matches()) {
            moderationEventService.recordBlockedEvent("skill", skill, "invalid-skill-pattern");
            throw new ModerationViolationException("skill contains invalid characters");
        }

        return normalized;
    }

    public String normalizeMessage(String message) {
        ensureSafeText("message", message);
        return message.trim();
    }

    private boolean containsBlockedContent(String text) {
        return BLOCKED_TERMS.stream().anyMatch(text::contains);
    }

    private boolean containsLink(String text) {
        return text.contains("http://") || text.contains("https://") || text.contains("www.");
    }
}