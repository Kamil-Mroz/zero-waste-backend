package com.kamilpm.zero_waste.domain.dto;

public record ProfileQueryData(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {
}
