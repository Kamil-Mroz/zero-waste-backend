package com.kamilpm.zero_waste.image.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "pending_image_uploads")
public class PendingImageUpload {
  @Id
  private UUID id;

  @Column (nullable = false)
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

  @Enumerated (EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  public enum Status {
    PENDING,
    ATTACHED
  }
}
