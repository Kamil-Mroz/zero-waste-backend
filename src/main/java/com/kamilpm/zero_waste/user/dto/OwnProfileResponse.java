package com.kamilpm.zero_waste.user.dto;

import lombok.Builder;

@Builder
public record OwnProfileResponse(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {

}
