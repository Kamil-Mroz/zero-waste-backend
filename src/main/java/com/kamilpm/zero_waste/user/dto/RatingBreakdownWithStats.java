package com.kamilpm.zero_waste.user.dto;

public record RatingBreakdownWithStats(
    long oneStar,
    long twoStar,
    long threeStar,
    long fourStar,
    long fiveStar,
    Long count,
    Double avg) {

}
