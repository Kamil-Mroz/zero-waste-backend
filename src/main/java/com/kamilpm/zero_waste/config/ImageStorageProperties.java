package com.kamilpm.zero_waste.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@ConfigurationProperties
@Component
@AllArgsConstructor
@Data
public class ImageStorageProperties {
  private String basePath;
  private Set<String> allowedMimeTypes;
  private Set<String> allowedExtensions;

  public ImageStorageProperties() {
    this.basePath = "./images";
    this.allowedMimeTypes = Set.of("image/jpeg", "image/png");
    this.allowedExtensions = Set.of("jpg", "jpeg", "png");
  }

}
