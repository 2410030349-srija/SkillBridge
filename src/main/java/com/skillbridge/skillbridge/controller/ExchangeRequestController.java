package com.skillbridge.skillbridge.controller; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.CreateExchangeRequest;
import com.skillbridge.skillbridge.dto.RequestStatusUpdate;
import com.skillbridge.skillbridge.model.ExchangeRequest;
import com.skillbridge.skillbridge.service.ExchangeRequestService;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests")
@Validated
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;
    private final UserService userService;

    public ExchangeRequestController(ExchangeRequestService exchangeRequestService, UserService userService) {
        this.exchangeRequestService = exchangeRequestService;
        this.userService = userService;
    }

    @PostMapping
    public ExchangeRequest createRequest(@Valid @RequestBody CreateExchangeRequest request, Authentication authentication) {
        Long senderId = userService.requireUserByEmail(authentication.getName()).getId();
        return exchangeRequestService.createRequest(
                senderId,
                request.receiverId(),
                request.requestedSkill(),
                request.note());
    }

    @PutMapping("/{requestId}/status")
    public ExchangeRequest updateStatus(@PathVariable Long requestId, @Valid @RequestBody RequestStatusUpdate request) {
        return exchangeRequestService.updateStatus(requestId, request.status());
    }

    @PutMapping("/{requestId}/cancel")
    public ExchangeRequest cancelRequest(@PathVariable Long requestId) {
        return exchangeRequestService.cancelRequest(requestId);
    }

    @GetMapping
    public List<ExchangeRequest> listRequestsForUser(@RequestParam(required = false) Long userId, Authentication authentication) {
        Long resolvedUserId = userService.requireUserByEmail(authentication.getName()).getId();
        return exchangeRequestService.listForUser(resolvedUserId);
    }
}