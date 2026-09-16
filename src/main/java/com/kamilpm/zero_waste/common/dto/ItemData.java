package com.kamilpm.zero_waste.common.dto;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record ItemData(UUID id,
    String title,
    String description,
    String city,
    ItemCondition condition,
    ItemState state,
    ModerationStatus moderationStatus,
    CategoryData category,
    UserSummaryDto owner,
    List<ImageData> images,
    ImageData thumbnail) {

}
