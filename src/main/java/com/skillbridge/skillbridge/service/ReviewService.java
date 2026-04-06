package com.skillbridge.skillbridge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillbridge.skillbridge.model.ExchangeRequest;
import com.skillbridge.skillbridge.model.RequestStatus;
import com.skillbridge.skillbridge.model.Review;
import com.skillbridge.skillbridge.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ExchangeRequestService exchangeRequestService;
    private final UserService userService;
    private final ContentModerationService moderationService;

    public ReviewService(ReviewRepository reviewRepository, ExchangeRequestService exchangeRequestService, UserService userService, ContentModerationService moderationService) {
        this.reviewRepository = reviewRepository;
        this.exchangeRequestService = exchangeRequestService;
        this.userService = userService;
        this.moderationService = moderationService;
    }

    public Review addReview(Long requestId, Long reviewerId, Long reviewedUserId, Integer rating, String feedback) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        ExchangeRequest request = exchangeRequestService.requireRequest(requestId);
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted requests can be reviewed");
        }

        if (!isParticipant(request, reviewerId) || !isParticipant(request, reviewedUserId)) {
            throw new IllegalArgumentException("Reviewer and reviewed user must be part of the request");
        }

        if (reviewerId.equals(reviewedUserId)) {
            throw new IllegalArgumentException("You cannot review yourself");
        }

        Review review = new Review();
        review.setExchangeRequest(request);
        review.setReviewer(userService.requireUserById(reviewerId));
        review.setReviewedUser(userService.requireUserById(reviewedUserId));
        review.setRating(rating);
        review.setFeedback(moderationService.normalizeMessage(feedback));
        return reviewRepository.save(review);
    }

    public List<Review> listForUser(Long reviewedUserId) {
        userService.requireUserById(reviewedUserId);
        return reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(reviewedUserId);
    }

    private boolean isParticipant(ExchangeRequest request, Long userId) {
        return request.getSender().getId().equals(userId) || request.getReceiver().getId().equals(userId);
    }
}