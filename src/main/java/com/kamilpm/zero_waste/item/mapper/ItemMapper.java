package com.kamilpm.zero_waste.item.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.CategoryData;
import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.common.dto.SimpleItemData;
import com.kamilpm.zero_waste.common.dto.UserSummaryDto;
import com.kamilpm.zero_waste.item.dto.ItemDto;
import com.kamilpm.zero_waste.item.dto.ItemListDto;
import com.kamilpm.zero_waste.item.entity.Item;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ItemMapper {

  public ItemDto toDto(Item item, CategoryData category, List<ImageData> images, ImageData thumbnail) {
    if (item == null)
      return null;

    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), category, null, images, thumbnail);
  };

  public ItemListDto toListDto(Item item, CategoryData category, ImageData thumbnail) {
    if (item == null)
      return null;

    return new ItemListDto(item.getId(), item.getTitle(), item.getCity(), item.getCondition(), item.getState(),
        item.getModerationStatus(),
        category, null, thumbnail);
  };

  public ItemListDto toListDto(Item item, CategoryData category, ImageData thumbnail, UserSummaryDto user) {
    if (item == null)
      return null;

    return new ItemListDto(item.getId(), item.getTitle(), item.getCity(), item.getCondition(), item.getState(),
        item.getModerationStatus(),
        category, user, thumbnail);
  };

  public ItemDto toDtoWithOwner(Item item, CategoryData category, List<ImageData> images, ImageData thumbnail,
      UserSummaryDto userSummary) {

    if (item == null)
      return null;
    return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), category, userSummary, images, thumbnail);
  };

  public SimpleItemData toSimpleItemData(Item item, UserSummaryDto user) {
    if (item == null)
      return null;

    return new SimpleItemData(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), item.getOwnerId(), user);
  }

  public SimpleItemData toSimpleItemData(Item item) {
    if (item == null)
      return null;

    return new SimpleItemData(item.getId(), item.getTitle(), item.getDescription(), item.getCity(), item.getCondition(),
        item.getState(), item.getModerationStatus(), item.getOwnerId(), null);
  }

}
