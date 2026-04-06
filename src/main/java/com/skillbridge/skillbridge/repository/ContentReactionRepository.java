package com.skillbridge.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.skillbridge.model.ContentReaction;
import com.skillbridge.skillbridge.model.ReactionType;

@Repository
public interface ContentReactionRepository extends JpaRepository<ContentReaction, Long> {

    Optional<ContentReaction> findByUserIdAndContentId(Long userId, Long contentId);

    List<ContentReaction> findByUserId(Long userId);

    long countByContentIdAndReactionType(Long contentId, ReactionType reactionType);

    @Transactional
    void deleteByContentId(Long contentId);
}
