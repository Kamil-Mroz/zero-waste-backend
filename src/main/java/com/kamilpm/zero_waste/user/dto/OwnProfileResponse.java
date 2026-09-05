package com.kamilpm.zero_waste.user.dto;

import com.kamilpm.zero_waste.user.api.ProfileItemSummary;
import com.kamilpm.zero_waste.user.api.ProfileReviewSummary;

import lombok.Builder;

@Builder
public record OwnProfileResponse(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {

}
