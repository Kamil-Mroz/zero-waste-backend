package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Item;

@Mapper(componentModel = "spring", uses = { UserMapper.class, CategoryMapper.class, ImageMapper.class })
public interface ItemMapper {

  @Mapping(target = "owner", expression = "java(null)")
  ItemDto toDto(Item item);

  ItemDto toDtoWithOwner(Item item);

  @Named("itemWithOwner")
  default ItemDto toItemDtoWithOwner(Item item) {
    return toDtoWithOwner(item);
  }

}
