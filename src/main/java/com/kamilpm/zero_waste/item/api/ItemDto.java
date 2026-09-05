package com.kamilpm.zero_waste.item.api;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.category.api.CategoryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.image.api.ImageDto;
import com.kamilpm.zero_waste.item.entity.ItemCondition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
  private UUID id;
  private String title;
  private String description;
  private String city;
  private ItemCondition condition;
  private ItemState state;
  private ModerationStatus moderationStatus;
  private CategoryDto category;
  private UserSummaryDto owner;
  private List<ImageDto> images;
  private ImageDto thumbnail;
}
