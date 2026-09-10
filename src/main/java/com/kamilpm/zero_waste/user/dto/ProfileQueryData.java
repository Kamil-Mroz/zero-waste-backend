package com.kamilpm.zero_waste.user.dto;

public record ProfileQueryData(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {
}
