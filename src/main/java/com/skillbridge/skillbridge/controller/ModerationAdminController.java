package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.model.ModerationEvent;
import com.skillbridge.skillbridge.service.AdminAccessService;
import com.skillbridge.skillbridge.service.ModerationEventService;

@RestController
@RequestMapping("/admin/moderation-events")
public class ModerationAdminController {

    private final ModerationEventService moderationEventService;
    private final AdminAccessService adminAccessService;

    public ModerationAdminController(ModerationEventService moderationEventService, AdminAccessService adminAccessService) {
        this.moderationEventService = moderationEventService;
        this.adminAccessService = adminAccessService;
    }

    @GetMapping
    public List<ModerationEvent> listEvents(@RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        adminAccessService.validateAdminKey(adminKey);
        return moderationEventService.listRecentEvents();
    }
}