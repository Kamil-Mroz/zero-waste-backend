package com.kamilpm.zero_waste.domain.dto;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.ItemCondition;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.entity.ModerationStatus;

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
