package com.kamilpm.zero_waste.item.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.category.api.CategoryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.image.api.ImageDto;
import com.kamilpm.zero_waste.item.api.ItemState;
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
