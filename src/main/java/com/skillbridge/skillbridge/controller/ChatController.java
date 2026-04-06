package com.skillbridge.skillbridge.controller; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ChatMessageRequest;
import com.skillbridge.skillbridge.model.ChatMessage;
import com.skillbridge.skillbridge.service.ChatService;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests/{requestId}/chat")
@Validated
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @PostMapping
    public ChatMessage addMessage(@PathVariable Long requestId, @Valid @RequestBody ChatMessageRequest request, Authentication authentication) {
        Long senderId = userService.requireUserByEmail(authentication.getName()).getId();
        return chatService.addMessage(requestId, senderId, request.message());
    }

    @GetMapping
    public List<ChatMessage> listMessages(@PathVariable Long requestId, @RequestParam(required = false) Long userId, Authentication authentication) {
        Long resolvedUserId = userService.requireUserByEmail(authentication.getName()).getId();
        return chatService.listMessages(requestId, resolvedUserId);
    }
}