package com.kamilpm.zero_waste.service.impl;

import com.kamilpm.zero_waste.config.ImageStorageProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.domain.entity.Image;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.exception.ApiException;
import com.kamilpm.zero_waste.repository.ImageRepository;
import com.kamilpm.zero_waste.service.ImageService;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;
  private final ImageStorageProperties properties;
  private Path rootPath;

  @PostConstruct
  void init() throws IOException {
    this.rootPath = Paths.get(properties.getBasePath());
    Files.createDirectories(rootPath);
  }

  @Override
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

  @Override
  @Transactional
  public void uploadItemImages(Item item, List<MultipartFile> files) {

    if (files == null)
      return;
    List<Image> images = new ArrayList<>();

    try {
      LocalDate today = LocalDate.now();
      Path dateDirectory = rootPath.resolve(
          today.getYear() + File.separator + String.format("%02d", today.getMonthValue()) + File.separator
              + String.format("%02d", today.getDayOfMonth()));
      Files.createDirectories(dateDirectory);

      for (MultipartFile file : files) {
        byte[] bytes = file.getBytes();
        validateImage(bytes, file);

        String ext = getFileExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Path target = dateDirectory.resolve(storedName);
        Files.write(target, bytes);

        images.add(
            Image.builder()
                .item(item)
                .originalName(file.getOriginalFilename())
                .storedName(rootPath.relativize(target).toString())
                .mimeType(file.getContentType())
                .size(file.getSize())
                .createdAt(Instant.now())
                .build());
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);

    }

    imageRepository.saveAll(images);
  }

  private void validateImage(byte[] bytes, MultipartFile file) {
    if (bytes.length == 0) {
      throw new ApiException("File is empty", HttpStatus.BAD_REQUEST);
    }

    String extension = getFileExtension(file.getOriginalFilename());

    if (!properties.getAllowedExtensions().contains(extension)) {
      throw new ApiException("Invalid file extension", HttpStatus.BAD_REQUEST);
    }
    String detectedType = detectMagicType(Arrays.copyOf(bytes, 12));

    validateMatch(extension, detectedType);

    String mimeType = file.getContentType();
    if (mimeType == null || !properties.getAllowedMimeTypes().contains(mimeType)) {
      throw new ApiException("Invalid mime type.", HttpStatus.BAD_REQUEST);
    }

    validateMatch(extension, mimeType);
  }

  private String detectMagicType(byte[] bytes) {
    if (isJpeg(bytes)) {
      return "image/jpeg";
    }
    if (isPng(bytes)) {
      return "image/png";
    }
    throw new RuntimeException("Unknown file type");
  }

  private boolean isJpeg(byte[] b) {
    return b.length >= 4 &&
        (b[0] & 0xFF) == 0xFF &&
        (b[1] & 0xFF) == 0xD8 &&
        (b[2] & 0xFF) == 0xFF;
  }

  private boolean isPng(byte[] b) {
    return b.length >= 8 &&
        (b[0] & 0xFF) == 0x89 &&
        b[1] == 0x50 &&
        b[2] == 0x4E &&
        b[4] == 0x47 &&
        b[5] == 0x0D &&
        b[6] == 0x0A &&
        b[7] == 0x1A &&
        b[8] == 0x0A;
  }

  private void validateMatch(String extension, String mime) {
    boolean valid = switch (extension) {
      case "jpg", "jpeg" -> mime.equals("image/jpeg");
      case "png" -> mime.equals("image/png");
      default -> false;
    };

    if (!valid) {
      throw new ApiException("Extension mismatch", HttpStatus.BAD_REQUEST);
    }

  }

  private String getFileExtension(String fileName) {
    if (fileName == null) {
      return "";
    }

    int lastDot = fileName.lastIndexOf('.');
    return lastDot == -1 ? "" : fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);

  }

  @Override
  @Transactional
  public void deleteImages(UUID itemId, List<UUID> imageIds) {
    List<Image> images = imageRepository.findByItem_IdAndIdIn(itemId, imageIds);

    deleteImagesFromDisk(images);

    imageRepository.deleteImagesByItem(itemId, imageIds);
  }

  @Override
  public void deleteImagesFromDisk(List<Image> images) {
    for (Image image : images) {
      try {
        Path filePath = rootPath.resolve(image.getStoredName()).normalize().toAbsolutePath();
        Path normalizedRoot = rootPath.normalize().toAbsolutePath();
        if (!filePath.startsWith(normalizedRoot)) {
          throw new Exception("Because not root path");
        }
        Files.deleteIfExists(filePath);

      } catch (Exception ex) {
        throw new ApiException("Failed to delete file: " + image.getStoredName() + " " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR);

      }

    }
  }

  @Override
  @Transactional
  public void deleteImagesByItemId(UUID itemId) {
    imageRepository.deleteAllByItemId(itemId);

  }
}
