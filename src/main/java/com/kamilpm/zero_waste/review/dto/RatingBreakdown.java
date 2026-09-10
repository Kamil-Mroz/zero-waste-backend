package com.kamilpm.zero_waste.review.dto;

import lombok.Builder;

@Builder
public record RatingBreakdown(
    long oneStar,
    long twoStar,
    long threeStar,
    long fourStar,
    long fiveStar) {

}
