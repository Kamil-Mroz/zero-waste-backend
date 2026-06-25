package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;

import com.kamilpm.zero_waste.domain.dto.BlogDto;
import com.kamilpm.zero_waste.domain.entity.Blog;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface BlogMapper {
  BlogDto toDto(Blog blog);
}
