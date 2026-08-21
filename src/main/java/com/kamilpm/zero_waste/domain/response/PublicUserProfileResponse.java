package com.kamilpm.zero_waste.domain.response;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.dto.ProfileItemSummary;
import com.kamilpm.zero_waste.domain.dto.ProfileReviewSummary;

import lombok.Builder;

@Builder
public record PublicUserProfileResponse(
    UUID id,
    String nickname,
    Instant joinedAt,
    Boolean banned,
    ProfileItemSummary items,
    ProfileReviewSummary reviews) {

}
