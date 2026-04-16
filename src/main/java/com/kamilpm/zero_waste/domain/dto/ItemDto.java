package com.kamilpm.zero_waste.domain.dto;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.ItemCondition;
import com.kamilpm.zero_waste.domain.entity.ItemState;

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
  private CategoryDto category;
  private UserDto owner;
}
