package com.kamilpm.zero_waste.domain.request;

import java.util.UUID;

import com.kamilpm.zero_waste.domain.entity.ItemCondition;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

  @NotBlank(message = "Title is required")
  private String title;
  @NotBlank(message = "Description is required")
  private String description;
  private ItemCondition condition;
  @NotBlank(message = "City is required")
  private String city;
  private UUID categoryId;
}
