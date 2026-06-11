package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.mapper.CategoryMapper;
import com.kamilpm.zero_waste.domain.mapper.ImageMapper;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemMapperImpl implements ItemMapper {
  private final CategoryMapper categoryMapper;
  private final UserMapper userMapper;
  private final ImageMapper imageMapper;

  @Override
  public ItemDto toDto(Item item) {
    return ItemDto.builder()
        .id(item.getId())
        .title(item.getTitle())
        .description(item.getDescription())
        .state(item.getState())
        .condition(item.getCondition())
        .city(item.getCity())
        .category(categoryMapper.toDto(item.getCategory()))
        .images(item.getImages().stream().map(imageMapper::toDto).toList())
        .build();
  }

  @Override
  public ItemDto toDtoWithOwner(Item item) {
    return ItemDto.builder()
        .id(item.getId())
        .title(item.getTitle())
        .description(item.getDescription())
        .state(item.getState())
        .condition(item.getCondition())
        .city(item.getCity())
        .category(categoryMapper.toDto(item.getCategory()))
        .owner(userMapper.toDto(item.getOwner()))
        .images(item.getImages().stream().map(imageMapper::toDto).toList())
        .build();
  }


}
