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

    return new CategoryDto(category.getId(), category.getName(), categoryParentId(category));

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
    return new CategoryTreeDto(category.getId(), category.getName(), null);
  }

  private UUID categoryParentId(Category category) {
    Category parent = category.getParent();
    if (parent == null) {
      return null;
    }
    return parent.getId();
  }

}
