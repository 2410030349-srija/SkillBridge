package com.skillbridge.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.skillbridge.model.ContentBookmark;

@Repository
public interface ContentBookmarkRepository extends JpaRepository<ContentBookmark, Long> {

    boolean existsByUserIdAndContentId(Long userId, Long contentId);

    List<ContentBookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Transactional
    void deleteByUserIdAndContentId(Long userId, Long contentId);

    @Transactional
    void deleteByContentId(Long contentId);
}
