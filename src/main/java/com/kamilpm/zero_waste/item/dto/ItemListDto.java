package com.kamilpm.zero_waste.item.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.CategoryData;
import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;

public record ItemListDto(
    UUID id,
    String title,
    String city,
    ItemCondition condition,
    ItemState state,
    CategoryData category,
    UserSummaryDto owner,
    ImageData thumbnail) {

}
