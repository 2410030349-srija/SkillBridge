package com.skillbridge.skillbridge.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ContentFeedbackRequest;
import com.skillbridge.skillbridge.dto.ContentUpsertRequest;
import com.skillbridge.skillbridge.dto.ContentView;
import com.skillbridge.skillbridge.dto.ReactionRequest;
import com.skillbridge.skillbridge.dto.UserFeedbackView;
import com.skillbridge.skillbridge.service.ContentPlatformService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/content")
@Validated
public class ContentController {

    private final ContentPlatformService contentPlatformService;

    public ContentController(ContentPlatformService contentPlatformService) {
        this.contentPlatformService = contentPlatformService;
    }

    @PostMapping
    public ContentView create(@Valid @RequestBody ContentUpsertRequest request, Authentication authentication) {
        return contentPlatformService.createContent(authentication.getName(), request);
    }

    @PutMapping("/{contentId}")
    public ContentView update(@PathVariable Long contentId, @Valid @RequestBody ContentUpsertRequest request, Authentication authentication) {
        return contentPlatformService.updateOwnContent(authentication.getName(), contentId, request);
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(@PathVariable Long contentId, Authentication authentication) {
        System.err.println("[CONTROLLER-DELETE] contentId=" + contentId + ", user=" + (authentication != null ? authentication.getName() : "null") + ", authenticated=" + (authentication != null && authentication.isAuthenticated()));
        if (authentication == null) {
            System.err.println("[CONTROLLER-DELETE] Authentication is NULL!");
            return ResponseEntity.status(403).build();
        }
        try {
            contentPlatformService.deleteOwnContent(authentication.getName(), contentId);
            System.err.println("[CONTROLLER-DELETE] Delete successful for contentId=" + contentId);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            System.err.println("[CONTROLLER-DELETE] Exception: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            throw ex;
        }
    }

    @GetMapping("/{contentId}")
    public ContentView details(@PathVariable Long contentId, Authentication authentication) {
        return contentPlatformService.getContentDetails(authentication.getName(), contentId);
    }

    @GetMapping("/search")
    public List<ContentView> search(@RequestParam String keyword, Authentication authentication) {
        return contentPlatformService.searchContent(authentication.getName(), keyword);
    }

    @PostMapping("/{contentId}/feedback")
    public ContentView feedback(@PathVariable Long contentId, @Valid @RequestBody ContentFeedbackRequest request, Authentication authentication) {
        return contentPlatformService.addOrUpdateFeedback(authentication.getName(), contentId, request);
    }

    @PostMapping("/{contentId}/reaction")
    public ContentView react(@PathVariable Long contentId, @Valid @RequestBody ReactionRequest request, Authentication authentication) {
        return contentPlatformService.react(authentication.getName(), contentId, request.type());
    }

    @PostMapping("/{contentId}/bookmark")
    public ResponseEntity<Void> bookmark(@PathVariable Long contentId, Authentication authentication) {
        contentPlatformService.bookmark(authentication.getName(), contentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{contentId}/bookmark")
    public ResponseEntity<Void> unbookmark(@PathVariable Long contentId, Authentication authentication) {
        contentPlatformService.removeBookmark(authentication.getName(), contentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookmarks")
    public List<ContentView> bookmarks(Authentication authentication) {
        return contentPlatformService.myBookmarks(authentication.getName());
    }

    @GetMapping("/mine")
    public List<ContentView> mine(Authentication authentication) {
        return contentPlatformService.myUploadedContent(authentication.getName());
    }

    @GetMapping("/feedback")
    public List<UserFeedbackView> myFeedback(Authentication authentication) {
        return contentPlatformService.myFeedback(authentication.getName());
    }
}
