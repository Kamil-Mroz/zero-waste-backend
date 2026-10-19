package com.kamilpm.zero_waste.image.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pending_image_uploads")
public class PendingImageUpload {
  @Id
  private UUID id;

  @Column(nullable = false)
  private UUID ownerId;

  @Column(nullable = false, unique = true)
  private String objectKey;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private String mimeType;

  @Column(nullable = false)
  private long size;

  @Column(nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  public enum Status {
    PENDING,
    ATTACHED
  }

  public PendingImageUpload() {
  }

  public PendingImageUpload(UUID id, UUID ownerId, String objectKey, String originalName, String mimeType, long size,
      Instant expiresAt, Status status) {
    this.id = id;
    this.ownerId = ownerId;
    this.objectKey = objectKey;
    this.originalName = originalName;
    this.mimeType = mimeType;
    this.size = size;
    this.expiresAt = expiresAt;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public String getObjectKey() {
    return objectKey;
  }

  public void setObjectKey(String objectKey) {
    this.objectKey = objectKey;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "PendingImageUpload [id=" + id + ", ownerId=" + ownerId + ", objectKey=" + objectKey + ", originalName="
        + originalName + ", mimeType=" + mimeType + ", size=" + size + ", expiresAt=" + expiresAt + ", status=" + status
        + "]";
  }

}
