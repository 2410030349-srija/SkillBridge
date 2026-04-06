package com.skillbridge.skillbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.skillbridge.model.ContentFeedback;

@Repository
public interface ContentFeedbackRepository extends JpaRepository<ContentFeedback, Long> {

    Optional<ContentFeedback> findByUserIdAndContentId(Long userId, Long contentId);

    List<ContentFeedback> findByContentId(Long contentId);

    List<ContentFeedback> findByUserIdOrderByIdDesc(Long userId);

    @Transactional
    void deleteByContentId(Long contentId);
}
