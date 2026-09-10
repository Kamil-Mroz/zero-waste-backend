package com.kamilpm.zero_waste.item.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.item.entity.ItemCondition;

public record ItemListDto(
    UUID id,
    String title,
    String city,
    ItemCondition condition,
    ItemState state,
    CategoryDto category,
    UserSummaryDto owner,
    ImageDto thumbnail) {

}
