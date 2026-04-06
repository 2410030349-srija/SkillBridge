package com.skillbridge.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillbridge.skillbridge.model.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByVerifiedTrueOrderByCreatedAtDesc();

    List<Content> findByDomainAndVerifiedTrueOrderByCreatedAtDesc(String domain);

    List<Content> findByUploadedByIdOrderByCreatedAtDesc(Long uploadedBy);

    List<Content> findByVerifiedFalseOrderByCreatedAtDesc();
}
