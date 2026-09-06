package com.kamilpm.zero_waste.item.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.category.api.CategoryDto;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.image.api.ImageDto;
import com.kamilpm.zero_waste.item.api.ItemDto;
import com.kamilpm.zero_waste.item.api.SimpleItemDto;
import com.kamilpm.zero_waste.item.dto.ItemListDto;
import com.kamilpm.zero_waste.item.entity.Item;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ItemMapper {

  public ItemDto toDto(Item item, CategoryDto category, List<ImageDto> images, ImageDto thumbnail) {
    if (item == null)
      return null;

    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), category, null, images, thumbnail);
  };

  public SimpleItemDto toSimpleDto(Item item) {
    if (item == null)
      return null;

    return new SimpleItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), item.getOwnerId());
  };

  public ItemListDto toListDto(Item item, CategoryDto category, ImageDto thumbnail) {
    if (item == null || category == null)
      return null;

    return new ItemListDto(item.getId(), item.getTitle(), item.getCity(), item.getCondition(), item.getState(),
        category, null, thumbnail);
  };

  public ItemDto toDtoWithOwner(Item item, CategoryDto category, List<ImageDto> images, ImageDto thumbnail,
      UserSummaryDto userSummary) {

    if (item == null)
      return null;
    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), category, userSummary, images, thumbnail);
  };

}
