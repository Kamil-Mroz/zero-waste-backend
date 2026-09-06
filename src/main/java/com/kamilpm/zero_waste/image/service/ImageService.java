package com.kamilpm.zero_waste.image.service;

import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.properties.ImageStorageProperties;
import com.kamilpm.zero_waste.image.repository.ImageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {
  private final ImageStorageProperties properties;
  private final ImageRepository imageRepository;
  private Path rootPath;

  @PostConstruct
  void init() throws IOException {
    this.rootPath = Paths.get(properties.getBasePath());
    Files.createDirectories(rootPath);
  }

  public Image getImageById(UUID id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Image not found"));
  }

  @Transactional(readOnly = true)
  public Optional<Resource> getImageAsResource(String storedName) {
    Path filePath = rootPath.resolve(storedName).normalize().toAbsolutePath();
    Path normalizedRoot = rootPath.normalize().toAbsolutePath();
    if (!filePath.startsWith(normalizedRoot)) {
      return Optional.empty();
    }
    if (!Files.exists(filePath)) {
      return Optional.empty();
    }
    try {
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists() || resource.isReadable()) {
        return Optional.of(resource);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {

      return Optional.empty();
    }
  }

}
