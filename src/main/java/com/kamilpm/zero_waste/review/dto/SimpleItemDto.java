package com.kamilpm.zero_waste.review.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record SimpleItemDto(
    UUID id,
    String title,
    String description,
    String city,
    ItemCondition condition,
    ItemState state,
    ModerationStatus moderationStatus,
    UUID ownerId

) {
}
