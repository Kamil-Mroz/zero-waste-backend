package com.kamilpm.zero_waste.image.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.common.exception.ApiException;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.mapper.ImageMapper;
import com.kamilpm.zero_waste.image.properties.ImageStorageProperties;
import com.kamilpm.zero_waste.image.repository.ImageRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageItemApi {

  private final ImageStorageProperties properties;
  private final ImageRepository imageRepository;
  private final ImageMapper imageMapper;
  private Path rootPath;

  @PostConstruct
  void init() throws IOException {
    this.rootPath = Paths.get(properties.getBasePath());
    Files.createDirectories(rootPath);
  }

  @Transactional
  public List<ImageDto> uploadItemImages(UUID itemId, List<MultipartFile> files) {

    if (files == null)
      return List.of();
    List<Image> images = new ArrayList<>();

    try {
      LocalDate today = LocalDate.now();
      Path dateDirectory = rootPath.resolve(
          today.getYear() + File.separator + String.format("%02d", today.getMonthValue()) + File.separator
              + String.format("%02d", today.getDayOfMonth()));
      Files.createDirectories(dateDirectory);

      Instant now = Instant.now();

      for (MultipartFile file : files) {
        byte[] bytes = file.getBytes();
        validateImage(bytes, file);

        String ext = getFileExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Path target = dateDirectory.resolve(storedName);
        Files.write(target, bytes);

        images.add(
            Image.builder()
                .itemId(itemId)
                .originalName(file.getOriginalFilename())
                .storedName(rootPath.relativize(target).toString())
                .mimeType(file.getContentType())
                .size(file.getSize())
                .createdAt(now)
                .build());
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return imageRepository.saveAll(images).stream().map(imageMapper::toDto).toList();
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
        b[3] == 0x47 &&
        b[4] == 0x0D &&
        b[5] == 0x0A &&
        b[6] == 0x1A &&
        b[7] == 0x0A;
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

  @Transactional
  public void deleteItemImages(UUID itemId, List<UUID> imageIds) {

    if (imageIds == null || imageIds.isEmpty()) {
      return;
    }
    List<Image> images = imageRepository.findByItemIdAndIdIn(itemId, imageIds);

    deleteImagesFromDisk(images);

    imageRepository.deleteImagesByItem(itemId, imageIds);
  }

  @Transactional
  public void deleteImagesByItems(Collection<UUID> itemIds) {

    if (itemIds == null || itemIds.isEmpty()) {
      return;
    }

    List<Image> images = imageRepository.findByItemIdIn(itemIds);

    deleteImagesFromDisk(images);

    imageRepository.deleteAll(images);
  }

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

  public List<ImageDto> getImagesByIds(List<UUID> ids) {
    return imageRepository.findAllById(ids).stream().map(imageMapper::toDto).toList();
  }

  public List<ImageDto> getImagesByItemId(UUID id) {
    return imageRepository.findAllByItemId(id).stream().map(imageMapper::toDto).toList();

  }

  public Map<UUID, ImageDto> getImagesByIds(Collection<UUID> ids) {
    return imageRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((image) -> image.getId(), imageMapper::toDto));
  }

}
