package com.kamilpm.zero_waste.user.dto;

import lombok.Builder;

@Builder
public record RatingBreakdownWithStats(
    long oneStar,
    long twoStar,
    long threeStar,
    long fourStar,
    long fiveStar,
    Long count,
    Double avg) {

}
