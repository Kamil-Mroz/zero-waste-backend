package com.kamilpm.zero_waste.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.user.api.ProfileItemSummary;
import com.kamilpm.zero_waste.user.api.ProfileReviewSummary;

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
