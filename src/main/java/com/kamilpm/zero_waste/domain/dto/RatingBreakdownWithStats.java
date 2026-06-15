package com.kamilpm.zero_waste.domain.dto;

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
