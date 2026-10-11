package com.kamilpm.zero_waste.image.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kamilpm.zero_waste.common.dto.ImageData;
import com.kamilpm.zero_waste.common.events.DeleteImagesEvent;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.common.interfaces.ImageProvider;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.entity.PendingImageUpload;
import com.kamilpm.zero_waste.image.mapper.ImageMapper;
import com.kamilpm.zero_waste.image.properties.ImageStorageProperties;
import com.kamilpm.zero_waste.image.repository.ImageRepository;
import com.kamilpm.zero_waste.image.repository.PendingImageUploadRepository;
import com.kamilpm.zero_waste.properties.AWSProperties;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageItemApi implements ImageProvider {

  private final ImageStorageProperties properties;
  private final ImageRepository imageRepository;
  private final ImageMapper imageMapper;
  private Path rootPath;
  private final S3Client s3Client;
  private final AWSProperties awsProperties;
  private final PendingImageUploadRepository pendingImageUploadRepository;
  private final CurrentUserProvider currentUser;
  private final S3Presigner presigner;

  @PostConstruct
  void init() throws IOException {
    this.rootPath = Paths.get(properties.getBasePath());
    Files.createDirectories(rootPath);
  }

  @Override
  public List<ImageData> getAllImagesByIds(Collection<UUID> ids) {
    return imageRepository.findAllById(ids).stream()
        .map((image) -> imageMapper.toDataDto(image, createDownloadUrl(image.getStoredName()))).toList();
  }

  @Override
  public List<ImageData> getImagesByItemId(UUID id) {
    return imageRepository.findAllByItemId(id).stream()
        .map(image -> imageMapper.toDataDto(image, createDownloadUrl(image.getStoredName()))).toList();

  }

  @Override
  public Map<UUID, ImageData> getImagesByIds(Collection<UUID> ids) {
    return imageRepository.findAllById(ids).stream()
        .collect(Collectors.toMap((image) -> image.getId(),
            (image) -> imageMapper.toDataDto(image, createDownloadUrl(image.getStoredName()))));
  }

  @Override
  public void deleteImages(Collection<UUID> ids) {
    deleteImagesByIds(ids);
    imageRepository.flush();
  }

  @ApplicationModuleListener
  void on(DeleteImagesEvent event) {
    deleteImagesByIds(event.ids());
  }

  @Transactional
  public List<ImageData> attachUploadedImages(
      UUID itemId,
      List<UUID> uploadIds) {

    if (uploadIds == null || uploadIds.isEmpty()) {
      return List.of();
    }

    if (uploadIds.size() > 5
        || new HashSet<>(uploadIds).size() != uploadIds.size()) {
      throw new IllegalArgumentException(
          "Invalid image upload list");
    }

    UUID ownerId = currentUser
        .getRequiredAuthenticatedUser()
        .id();

    var pendingUploads = pendingImageUploadRepository.findAllByIdIn(uploadIds);

    if (pendingUploads.size() != uploadIds.size()) {
      throw new IllegalArgumentException(
          "One or more uploads do not exist");
    }

    var byId = pendingUploads.stream()
        .collect(Collectors.toMap(
            PendingImageUpload::getId,
            upload -> upload));

    Instant now = Instant.now();

    // Preserve the original frontend order.
    var orderedUploads = uploadIds.stream()
        .map(byId::get)
        .toList();

    for (var upload : orderedUploads) {
      if (!upload.getOwnerId().equals(ownerId)
          || upload.getStatus() != PendingImageUpload.Status.PENDING
          || !upload.getExpiresAt().isAfter(now)) {
        throw new IllegalArgumentException(
            "Upload is invalid or expired");
      }

      var head = s3Client.headObject(
          HeadObjectRequest.builder()
              .bucket(awsProperties.bucket())
              .key(upload.getObjectKey())
              .build());

      if (head.contentLength() == null
          || head.contentLength() != upload.getSize()
          || !upload.getMimeType().equals(head.contentType())) {
        throw new IllegalArgumentException(
            "Uploaded object metadata does not match");
      }
    }

    Instant createdAt = Instant.now();

    List<Image> images = orderedUploads.stream()
        .map(upload -> {
          upload.setStatus(
              PendingImageUpload.Status.ATTACHED);

          return Image.builder()
              .itemId(itemId)
              .originalName(upload.getOriginalName())
              .storedName(upload.getObjectKey())
              .mimeType(upload.getMimeType())
              .size(upload.getSize())
              .createdAt(createdAt)
              .build();
        })
        .toList();

    pendingImageUploadRepository.saveAll(orderedUploads);

    return imageRepository.saveAll(images).stream()
        .map(image -> imageMapper.toDataDto(image, createDownloadUrl(image.getStoredName())))
        .toList();
  }

  @Cacheable(cacheNames = "imagePresignedUrls", cacheManager = "imageCacheManager", key = "#objectKey")
  public String createDownloadUrl(String objectKey) {
    GetObjectRequest getRequest = GetObjectRequest.builder()
        .bucket(awsProperties.bucket())
        .key(objectKey)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofMinutes(30))
        .getObjectRequest(getRequest)
        .build();

    return presigner.presignGetObject(presignRequest)
        .url()
        .toString();
  }

  public void deleteObject(String objectKey) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(this.awsProperties.bucket()).key(objectKey).build());
  }

  private void deleteImagesByIds(Collection<UUID> ids) {
    List<Image> images = imageRepository.findAllById(ids);
    for (Image image : images) {
      deleteObject(image.getStoredName());
    }
    imageRepository.deleteAll(images);

  }
}
