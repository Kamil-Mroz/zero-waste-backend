package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.dto.CategoryDto;
import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;
import com.kamilpm.zero_waste.domain.mapper.CategoryMapper;

@Component
public class CategoryMapperImpl implements CategoryMapper {

  @Override
  public CategoryDto toDto(Category category) {

    return CategoryDto.builder().name(category.getName()).id(category.getId())
        .parentId(category.getParent() != null ? category.getParent().getId() : null)
        .build();
  }

  @Override
  public CategoryTreeDto toTreeDto(Category category) {
    return CategoryTreeDto.builder().id(category.getId()).name(category.getName()).build();
  }

}
