package com.kamilpm.zero_waste.item.dto;

import java.util.List;
import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ItemCondition;
import com.kamilpm.zero_waste.common.dto.ItemState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemRequest(

    @NotBlank(message = "Title is required") String title,
    @NotBlank(message = "Description is required") @Size(max = 255, message = "Description can not exceed  255 characters") String description,
    ItemCondition condition,
    ItemState state,
    @NotBlank(message = "City is required") String city,
    @NotNull(message = "Category ID is required") UUID categoryId,
    @Size(max = 5, message = "You can upload a maximum of 5 images")
    List<UUID> imageUploadIds,

    Integer thumbnailIndex

) {
}
