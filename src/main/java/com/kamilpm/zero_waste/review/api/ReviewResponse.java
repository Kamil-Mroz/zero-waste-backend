package com.kamilpm.zero_waste.review.api;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

import lombok.Builder;

@Builder
public record ReviewResponse(UUID id,
    int rating,
    String comment,
    UUID reviewerId,
    UUID revieweeId,
    String reviewerName,
    ModerationStatus moderationStatus,
    Instant createdAt) {
}
