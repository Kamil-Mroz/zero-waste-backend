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

public record UpdateItemRequest(
    @NotBlank(message = "Title is required") String title,
    @NotBlank(message = "Description is required") String description,
    @NotNull ItemCondition condition,
    @NotNull ItemState state,
    @NotBlank(message = "City is required") String city,
    @NotNull(message = "Category ID is required") UUID categoryId,
    @Size(max = 5, message = "You can upload a maximum of 5 images") List<UUID> imageUploadIds,
    Set<UUID> removedImageIds,
    Integer thumbnailIndex,
    UUID thumbnailExistingImageId) {

  public UpdateItemRequest {
    imageUploadIds = imageUploadIds != null ? imageUploadIds : new ArrayList<>();
    removedImageIds = removedImageIds != null ? removedImageIds : new HashSet<>();
  }
}
