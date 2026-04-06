package com.skillbridge.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillbridge.skillbridge.model.SearchHistory;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop20ByUserIdOrderBySearchedAtDesc(Long userId);
}
