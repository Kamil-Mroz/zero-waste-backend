package com.kamilpm.zero_waste.user.dto;

import com.kamilpm.zero_waste.user.api.ProfileItemSummary;
import com.kamilpm.zero_waste.user.api.ProfileReviewSummary;

public record ProfileQueryData(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {
}
