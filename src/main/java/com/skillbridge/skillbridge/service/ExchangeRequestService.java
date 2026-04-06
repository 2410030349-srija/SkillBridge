package com.skillbridge.skillbridge.service; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.model.ExchangeRequest;
import com.skillbridge.skillbridge.model.RequestStatus;
import com.skillbridge.skillbridge.model.User;
import com.skillbridge.skillbridge.repository.ExchangeRequestRepository;

@Service
public class ExchangeRequestService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;
    private final ContentModerationService moderationService;

    public ExchangeRequestService(ExchangeRequestRepository exchangeRequestRepository, UserService userService, ContentModerationService moderationService) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.userService = userService;
        this.moderationService = moderationService;
    }

    public ExchangeRequest createRequest(Long senderId, Long receiverId, String requestedSkill, String note) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send a request to yourself");
        }

        User sender = userService.requireUserById(senderId);
        User receiver = userService.requireUserById(receiverId);

        ExchangeRequest request = new ExchangeRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setRequestedSkill(moderationService.normalizeSkill(requestedSkill));
        request.setNote(moderationService.normalizeMessage(note));
        request.setStatus(RequestStatus.PENDING);
        return exchangeRequestRepository.save(request);
    }

    public ExchangeRequest updateStatus(Long requestId, RequestStatus status) {
        if (status == RequestStatus.CANCELLED) {
            throw new IllegalArgumentException("Use cancel endpoint for cancellation");
        }

        ExchangeRequest request = requireRequest(requestId);
        request.setStatus(status);
        return exchangeRequestRepository.save(request);
    }

    public ExchangeRequest cancelRequest(Long requestId) {
        ExchangeRequest request = requireRequest(requestId);
        request.setStatus(RequestStatus.CANCELLED);
        return exchangeRequestRepository.save(request);
    }

    public List<ExchangeRequest> listForUser(Long userId) {
        userService.requireUserById(userId);
        return exchangeRequestRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);
    }

    public ExchangeRequest requireRequest(Long requestId) {
        return exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }
}