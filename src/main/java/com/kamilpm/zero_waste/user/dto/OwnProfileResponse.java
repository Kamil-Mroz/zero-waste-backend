package com.kamilpm.zero_waste.user.dto;

import com.kamilpm.zero_waste.common.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.common.dto.ProfileReviewSummary;

import lombok.Builder;

@Builder
public record OwnProfileResponse(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {

}
