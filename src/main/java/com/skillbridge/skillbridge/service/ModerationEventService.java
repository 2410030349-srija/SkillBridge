package com.skillbridge.skillbridge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.model.ModerationEvent;
import com.skillbridge.skillbridge.repository.ModerationEventRepository;

@Service
public class ModerationEventService {

    private final ModerationEventRepository moderationEventRepository;

    public ModerationEventService(ModerationEventRepository moderationEventRepository) {
        this.moderationEventRepository = moderationEventRepository;
    }

    public void recordBlockedEvent(String fieldName, String input, String reason) {
        ModerationEvent event = new ModerationEvent();
        event.setFieldName(fieldName);
        event.setReason(reason);
        event.setInputPreview(buildPreview(input));
        moderationEventRepository.save(event);
    }

    public List<ModerationEvent> listRecentEvents() {
        return moderationEventRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private String buildPreview(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }

        String singleLine = value.replace("\n", " ").replace("\r", " ").trim();
        if (singleLine.length() <= 160) {
            return singleLine;
        }

        return singleLine.substring(0, 157) + "...";
    }
}