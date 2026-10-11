package com.kamilpm.zero_waste.image.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.image.entity.Image;

@Component
public class ImageMapper {

  public ImageData toDataDto(Image image, String url) {
    if (image == null) {
      return null;
    }
    return new ImageData(
        image.getId(),
        image.getOriginalName(),
        url);
  }
}
