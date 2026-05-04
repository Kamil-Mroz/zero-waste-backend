package com.kamilpm.zero_waste.domain.mapper.impl;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.domain.dto.ImageDto;
import com.kamilpm.zero_waste.domain.entity.Image;
import com.kamilpm.zero_waste.domain.mapper.ImageMapper;

@Component
public class ImageMapperImpl implements ImageMapper {

  @Override
  public ImageDto toDto(Image image) {

    return ImageDto.builder()
        .id(image.getId())
        .originalName(image.getOriginalName())
        .url("/api/v1/images/" + image.getId())
        .build();
  }

}
