package com.skillbridge.skillbridge.repository; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge.model.ModerationEvent;

public interface ModerationEventRepository extends JpaRepository<ModerationEvent, Long> {
    List<ModerationEvent> findTop100ByOrderByCreatedAtDesc();
}