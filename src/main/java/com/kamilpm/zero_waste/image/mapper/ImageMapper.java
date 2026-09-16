package com.kamilpm.zero_waste.image.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.image.dto.ImageDto;
import com.kamilpm.zero_waste.image.entity.Image;

@Component
public class ImageMapper {

  public ImageDto toDto(Image image) {
    if (image == null)
      return null;
    return new ImageDto(image.getId(), image.getOriginalName(), "/api/v1/images/" + image.getId());
  };

  public ImageData toDataDto(Image image) {
    if (image == null)
      return null;
    return new ImageData(image.getId(), image.getOriginalName(), "/api/v1/images/" + image.getId());
  };

}
