package com.kamilpm.zero_waste.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kamilpm.zero_waste.domain.entity.Review;
import com.kamilpm.zero_waste.domain.request.ReviewRequest;
import com.kamilpm.zero_waste.domain.response.ReviewResponse;

public interface ReviewService {
  Review createReview(ReviewRequest reviewRequest);

  Page<ReviewResponse> getReceivedReviews(Pageable pageable);

  Page<ReviewResponse> getGivenReviews(Pageable pageable);

  Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable);
}
