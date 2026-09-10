package com.kamilpm.zero_waste.offer.dto;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record ItemDto(
    UUID id,
    String title,
    String description,
    String city,
    ItemCondition condition,
    ItemState state,
    ModerationStatus moderationStatus,
    CategoryDto category,
    UserSummaryDto owner,
    List<ImageDto> images,
    ImageDto thumbnail) {
}
