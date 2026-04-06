package com.skillbridge.skillbridge.controller; // NOSONAR - false positive: package is named

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.skillbridge.dto.ReviewRequest;
import com.skillbridge.skillbridge.model.Review;
import com.skillbridge.skillbridge.service.ReviewService;
import com.skillbridge.skillbridge.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests/{requestId}/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @PostMapping
    public Review createReview(@PathVariable Long requestId, @Valid @RequestBody ReviewRequest request, Authentication authentication) {
        Long reviewerId = userService.requireUserByEmail(authentication.getName()).getId();
        return reviewService.addReview(
                requestId,
                reviewerId,
                request.reviewedUserId(),
                request.rating(),
                request.feedback());
    }

    @GetMapping("/user/{userId}")
    public List<Review> listForUser(@PathVariable Long userId) {
        return reviewService.listForUser(userId);
    }
}