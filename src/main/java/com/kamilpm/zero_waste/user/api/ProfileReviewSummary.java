package com.kamilpm.zero_waste.user.api;

import java.util.List;

import com.kamilpm.zero_waste.review.api.ReviewResponse;

import lombok.Builder;

@Builder
public record ProfileReviewSummary(
    double averageRating,
    long reviewCount,
    RatingBreakdown ratingBreakdown,
    List<ReviewResponse> latestReviews) {
}
