package com.kamilpm.zero_waste.domain.mapper;

import com.kamilpm.zero_waste.domain.dto.ImageDto;
import com.kamilpm.zero_waste.domain.entity.Image;

public interface ImageMapper {

  ImageDto toDto(Image image);

}
