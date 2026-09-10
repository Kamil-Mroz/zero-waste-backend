package com.kamilpm.zero_waste.user.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProfileReviewSummary(
    double averageRating,
    long reviewCount,
    RatingBreakdown ratingBreakdown,
    List<ReviewResponse> latestReviews) {
}
