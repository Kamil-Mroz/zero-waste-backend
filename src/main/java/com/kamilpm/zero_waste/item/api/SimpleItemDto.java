package com.kamilpm.zero_waste.item.api;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.item.entity.ItemCondition;

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
