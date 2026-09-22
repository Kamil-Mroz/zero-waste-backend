package com.kamilpm.zero_waste.common.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record SimpleItemData(
    UUID id,
    String title,
    String description,
    String city,
    ItemCondition condition,
    ItemState state,
    ModerationStatus moderationStatus,
    UUID ownerId,
    UserSummaryDto owner) {
}
