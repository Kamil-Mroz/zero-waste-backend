package com.kamilpm.zero_waste.domain.response;



import com.kamilpm.zero_waste.domain.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.domain.dto.ProfileReviewSummary;

import lombok.Builder;

@Builder
public record OwnProfileResponse(
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {

}
