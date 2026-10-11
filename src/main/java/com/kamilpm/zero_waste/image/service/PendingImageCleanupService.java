package com.kamilpm.zero_waste.image.service;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.image.entity.PendingImageUpload;
import com.kamilpm.zero_waste.image.repository.PendingImageUploadRepository;
import com.kamilpm.zero_waste.properties.AWSProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class PendingImageCleanupService {
  private final PendingImageUploadRepository repository;
  private final S3Client s3Client;
  private final AWSProperties awsProperties;

  @Scheduled(fixedDelay = 300_000)
  public void cleanupExpiredUploads() {
    var expired = repository.findAllByStatusAndExpiresAtBefore(PendingImageUpload.Status.PENDING, Instant.now());
    for (var upload : expired) {
      try {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(awsProperties.bucket())
            .key(upload.getObjectKey())
            .build());
        repository.delete(upload);
      } catch (Exception e) {

      }
    }
  }

}
