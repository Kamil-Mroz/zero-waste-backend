package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Item;

public interface ItemMapper {

  ItemDto toDto(Item item);
  ItemDto toDtoWithOwner(Item item);

}
