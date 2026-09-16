package com.kamilpm.zero_waste.common.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProfileReviewSummary(
    double averageRating,
    long reviewCount,
    RatingBreakdown ratingBreakdown,
    List<ReviewData> latestReviews) {
}
