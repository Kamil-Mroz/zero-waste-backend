package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.kamilpm.zero_waste.domain.dto.CategoryDto;
import com.kamilpm.zero_waste.domain.dto.CategoryTreeDto;
import com.kamilpm.zero_waste.domain.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(target = "parentId", source = "parent.id", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  CategoryDto toDto(Category category);

  CategoryTreeDto toTreeDto(Category category);

}
