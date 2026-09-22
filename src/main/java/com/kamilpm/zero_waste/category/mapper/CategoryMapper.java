package com.kamilpm.zero_waste.category.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.category.dto.CategoryDto;
import com.kamilpm.zero_waste.category.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.category.entity.Category;
import com.kamilpm.zero_waste.common.dto.CategoryData;

@Component
public class CategoryMapper {

  public CategoryDto toDto(Category category) {
    if (category == null) {
      return null;
    }

    CategoryDto.CategoryDtoBuilder categoryDto = CategoryDto.builder();

    categoryDto.parentId(categoryParentId(category));
    categoryDto.id(category.getId());
    categoryDto.name(category.getName());

    return categoryDto.build();
  }

  public CategoryData toDataDto(Category category) {
    if (category == null) {
      return null;
    }

    return new CategoryData(category.getId(), category.getName(), categoryParentId(category));
  }

  public CategoryTreeDto toTreeDto(Category category) {
    if (category == null) {
      return null;
    }

    CategoryTreeDto.CategoryTreeDtoBuilder categoryTreeDto = CategoryTreeDto.builder();

    categoryTreeDto.id(category.getId());
    categoryTreeDto.name(category.getName());

    return categoryTreeDto.build();
  }

  private UUID categoryParentId(Category category) {
    Category parent = category.getParent();
    if (parent == null) {
      return null;
    }
    return parent.getId();
  }

}
