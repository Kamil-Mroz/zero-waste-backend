package com.kamilpm.zero_waste.domain.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.kamilpm.zero_waste.domain.dto.ImageDto;
import com.kamilpm.zero_waste.domain.entity.Image;

@Mapper(componentModel = "spring")
public interface ImageMapper {

  @Mapping(target = "url", source = "id", qualifiedByName = "imageUrl")
  ImageDto toDto(Image image);

  @Named("imageUrl")
  default String toImageUrl(UUID id) {
    return "/api/v1/images/" + id;
  }

}
