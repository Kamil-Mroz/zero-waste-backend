package com.kamilpm.zero_waste.item.dto;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.CategoryData;
import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;

public record ItemDto(
    UUID id,
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
