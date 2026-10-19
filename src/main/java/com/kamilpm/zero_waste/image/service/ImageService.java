package com.kamilpm.zero_waste.image.service;

import com.kamilpm.zero_waste.common.exception.BadRequestException;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.interfaces.CurrentUserProvider;
import com.kamilpm.zero_waste.image.dto.ImageUploadRequest;
import com.kamilpm.zero_waste.image.dto.ImageUploadResponse;
import com.kamilpm.zero_waste.image.entity.Image;
import com.kamilpm.zero_waste.image.entity.PendingImageUpload;
import com.kamilpm.zero_waste.image.repository.ImageRepository;
import com.kamilpm.zero_waste.image.repository.PendingImageUploadRepository;
import com.kamilpm.zero_waste.properties.AWSProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Transactional
public class ImageService {
  private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
  private static final Map<String, String> ALLOWED_TYPES = Map.of(
      "image/jpeg", "jpg",
      "image/png", "png",
      "image/webp", "webp");

  private final ImageRepository imageRepository;
  private final S3Presigner presigner;
  private final AWSProperties awsProperties;
  private final PendingImageUploadRepository pendingImageUploadRepository;
  private final CurrentUserProvider currentUser;

  public ImageService(ImageRepository imageRepository, S3Presigner presigner, AWSProperties awsProperties,
      PendingImageUploadRepository pendingImageUploadRepository, CurrentUserProvider currentUser) {
    this.imageRepository = imageRepository;
    this.presigner = presigner;
    this.awsProperties = awsProperties;
    this.pendingImageUploadRepository = pendingImageUploadRepository;
    this.currentUser = currentUser;
  }

  public Image getImageById(UUID id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Image not found"));
  }

  @Transactional
  public ImageUploadResponse createUploadUrls(ImageUploadRequest request) {
    UUID ownerId = currentUser.getRequiredAuthenticatedUser().id();

    Instant now = Instant.now();
    Instant expiresAt = now.plus(Duration.ofMinutes(10));

    List<PendingImageUpload> pendingImageUploads = new ArrayList<>();

    List<ImageUploadResponse.UploadInstruction> uploads = request.files().stream()
        .map(file -> {
          String contentType = file.contentType();

          if (contentType == null) {
            throw new BadRequestException("Missing image content type");
          }

          contentType = contentType.toLowerCase(Locale.ROOT);

          if (!ALLOWED_TYPES.containsKey(contentType)) {
            throw new BadRequestException(
                "Unsupported image type: " + contentType);
          }

          if (file.size() <= 0 || file.size() > MAX_IMAGE_SIZE) {
            throw new BadRequestException(
                "Image must be between 1 byte and 5MB");
          }

          UUID uploadId = UUID.randomUUID();

          String objectKey = "%s/%s.%s".formatted(
              ownerId,
              uploadId,
              ALLOWED_TYPES.get(contentType));

          PendingImageUpload pending = new PendingImageUpload();
          pending.setId(uploadId);
          pending.setOwnerId(ownerId);
          pending.setObjectKey(objectKey);
          pending.setOriginalName(file.filename());
          pending.setMimeType(contentType);
          pending.setSize(file.size());
          pending.setExpiresAt(expiresAt);
          pending.setStatus(PendingImageUpload.Status.PENDING);

          pendingImageUploads.add(pending);

          PutObjectRequest putRequest = PutObjectRequest.builder()
              .bucket(awsProperties.bucket())
              .key(objectKey)
              .contentType(contentType)
              .build();

          PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(10))
              .putObjectRequest(putRequest)
              .build();

          String url = presigner
              .presignPutObject(presignRequest)
              .url()
              .toString();

          return new ImageUploadResponse.UploadInstruction(
              uploadId,
              url,
              "PUT",
              Map.of("Content-Type", contentType));
        })
        .toList();

    pendingImageUploadRepository.saveAll(pendingImageUploads);

    return new ImageUploadResponse(uploads);
  }

}
