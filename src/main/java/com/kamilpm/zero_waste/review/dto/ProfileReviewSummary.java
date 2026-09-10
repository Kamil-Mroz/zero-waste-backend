package com.kamilpm.zero_waste.review.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProfileReviewSummary(
    double averageRating,
    long reviewCount,
    RatingBreakdown ratingBreakdown,
    List<ReviewResponse> latestReviews) {
}
