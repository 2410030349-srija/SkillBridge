package com.skillbridge.skillbridge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.model.ChatMessage;
import com.skillbridge.skillbridge.model.ExchangeRequest;
import com.skillbridge.skillbridge.model.RequestStatus;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.repository.ChatMessageRepository;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ExchangeRequestService exchangeRequestService;
    private final UserService userService;
    private final ContentModerationService moderationService;

    public ChatService(ChatMessageRepository chatMessageRepository, ExchangeRequestService exchangeRequestService, UserService userService, ContentModerationService moderationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.exchangeRequestService = exchangeRequestService;
        this.userService = userService;
        this.moderationService = moderationService;
    }

    public ChatMessage addMessage(Long requestId, Long senderId, String message) {
        ExchangeRequest request = exchangeRequestService.requireRequest(requestId);
        User sender = userService.requireUserById(senderId);

        if (!isParticipant(request, senderId)) {
            throw new IllegalArgumentException("Sender is not part of this request");
        }

        if (request.getStatus() == RequestStatus.REJECTED || request.getStatus() == RequestStatus.CANCELLED) {
            throw new IllegalArgumentException("Chat is closed for this request");
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setExchangeRequest(request);
        chatMessage.setSender(sender);
        chatMessage.setMessage(moderationService.normalizeMessage(message));

        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> listMessages(Long requestId, Long userId) {
        ExchangeRequest request = exchangeRequestService.requireRequest(requestId);
        if (!isParticipant(request, userId)) {
            throw new IllegalArgumentException("User is not part of this request");
        }

        return chatMessageRepository.findByExchangeRequestIdOrderByCreatedAtAsc(requestId);
    }

    private boolean isParticipant(ExchangeRequest request, Long userId) {
        return request.getSender().getId().equals(userId) || request.getReceiver().getId().equals(userId);
    }
}