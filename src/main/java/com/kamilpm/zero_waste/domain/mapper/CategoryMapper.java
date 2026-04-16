package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.dto.CategoryDto;
import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;

public interface CategoryMapper {

  CategoryDto toDto(Category category);
  CategoryTreeDto toTreeDto(Category category);

}
