package com.kamilpm.zero_waste.domain.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ReviewResponse(UUID id,
    int rating,
    String comment,
    UUID reviewerId,
    String reviewerName,
    Instant createdAt) {
}
