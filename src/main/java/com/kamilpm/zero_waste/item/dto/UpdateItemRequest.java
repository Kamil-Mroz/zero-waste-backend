package com.kamilpm.zero_waste.item.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequest {
  @NotBlank(message = "Title is required")
  private String title;
  @NotBlank(message = "Description is required")
  private String description;
  @NotNull
  private ItemCondition condition;
  @NotNull
  private ItemState state;
  @NotBlank(message = "City is required")
  private String city;
  @NotNull(message = "Category ID is required")
  private UUID categoryId;
  @Size(max = 5, message = "You can upload a maximum of 5 images")

  @Builder.Default
  private List<UUID> imageUploadIds = new ArrayList<>();
  @Builder.Default
  private Set<UUID> removedImageIds = new HashSet<>();

  private Integer thumbnailIndex;
  private UUID thumbnailExistingImageId;
}
