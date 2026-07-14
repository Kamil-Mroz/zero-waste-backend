package com.kamilpm.zero_waste.domain.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.domain.entity.ItemCondition;
import com.kamilpm.zero_waste.domain.entity.ItemState;

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
  private ItemCondition condition;
  private ItemState state;
  @NotBlank(message = "City is required")
  private String city;
  @NotNull(message = "Category ID is required")
  private UUID categoryId;
  @Size(max = 5, message = "You can upload a maximum of 5 images")
  @Builder.Default
  private List<MultipartFile> images = new ArrayList<>();
  @Builder.Default
  private List<UUID> removedImageIds = new ArrayList<>();

  private Integer thumbnailIndex;
  private UUID thumbnailExistingImageId;
}
