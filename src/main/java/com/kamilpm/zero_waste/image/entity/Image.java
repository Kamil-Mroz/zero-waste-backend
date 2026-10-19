package com.kamilpm.zero_waste.image.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "images")
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "original_name", nullable = false)
  private String originalName;

  @Column(name = "stored_name", nullable = false)
  private String storedName;

  @Column(name = "mime_type", nullable = false)
  private String mimeType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @Column(name = "item_id", nullable = false)
  private UUID itemId;

  public Image() {
  }

  public Image(UUID id, String originalName, String storedName, String mimeType, Long size, Instant createdAt,
      UUID itemId) {
    this.id = id;
    this.originalName = originalName;
    this.storedName = storedName;
    this.mimeType = mimeType;
    this.size = size;
    this.createdAt = createdAt;
    this.itemId = itemId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public String getStoredName() {
    return storedName;
  }

  public void setStoredName(String storedName) {
    this.storedName = storedName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  @Override
  public String toString() {
    return "Image [id=" + id + ", originalName=" + originalName + ", storedName=" + storedName + ", mimeType="
        + mimeType + ", size=" + size + ", createdAt=" + createdAt + ", itemId=" + itemId + "]";
  }

}
