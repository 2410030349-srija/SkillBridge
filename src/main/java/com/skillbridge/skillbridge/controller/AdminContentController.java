package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ContentView;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.service.ContentPlatformService;
import com.skillbridge.skillbridge.service.UserService;

@RestController
@RequestMapping("/admin")
public class AdminContentController {

    private final ContentPlatformService contentPlatformService;
    private final UserService userService;

    public AdminContentController(ContentPlatformService contentPlatformService, UserService userService) {
        this.contentPlatformService = contentPlatformService;
        this.userService = userService;
    }

    @GetMapping("/content/pending")
    public List<ContentView> pending(Authentication authentication) {
        return contentPlatformService.pendingContentForAdmin(authentication.getName());
    }

    @PostMapping("/content/{contentId}/approve")
    public ContentView approve(@PathVariable Long contentId, Authentication authentication) {
        return contentPlatformService.approveContent(authentication.getName(), contentId);
    }

    @DeleteMapping("/content/{contentId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long contentId, Authentication authentication) {
        contentPlatformService.rejectContent(authentication.getName(), contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public List<User> users() {
        return userService.getAllUsers();
    }
}
