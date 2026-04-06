package com.skillbridge.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge.model.ExchangeRequest;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);
}