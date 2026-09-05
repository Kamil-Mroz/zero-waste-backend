package com.kamilpm.zero_waste.image.mapper;

import org.springframework.stereotype.Component;
import com.kamilpm.zero_waste.image.api.ImageDto;
import com.kamilpm.zero_waste.image.entity.Image;

@Component
public class ImageMapper {

  public ImageDto toDto(Image image) {
    if (image == null)
      return null;

    return new ImageDto(image.getId(), image.getOriginalName(), "/api/v1/images/" + image.getId());

  };

}
