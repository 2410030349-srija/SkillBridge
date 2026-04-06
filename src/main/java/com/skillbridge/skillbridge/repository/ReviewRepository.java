package com.skillbridge.skillbridge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillbridge.skillbridge.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewedUserIdOrderByCreatedAtDesc(Long reviewedUserId);
}