package com.skillbridge.skillbridge.repository; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByExchangeRequestIdOrderByCreatedAtAsc(Long requestId);
}