package com.kamilpm.zero_waste.common.dto;

import java.time.Instant;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

import lombok.Builder;

@Builder
public record ReviewData(UUID id,
    int rating,
    String comment,
    UUID reviewerId,
    UUID revieweeId,
    String reviewerName,
    ModerationStatus moderationStatus,
    Instant createdAt) {
}
