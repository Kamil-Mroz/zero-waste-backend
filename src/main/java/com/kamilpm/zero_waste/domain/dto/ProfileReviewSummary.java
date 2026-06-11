package com.kamilpm.zero_waste.domain.dto;

import java.util.List;

import com.kamilpm.zero_waste.domain.response.ReviewResponse;

import lombok.Builder;

@Builder
public record ProfileReviewSummary(
    double averageRating,
    long reviewCount,
    RatingBreakdown ratingBreakdown,
    List<ReviewResponse> latestReviews

) {
}
