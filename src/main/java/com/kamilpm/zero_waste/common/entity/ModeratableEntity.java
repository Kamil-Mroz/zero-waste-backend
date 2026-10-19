package com.kamilpm.zero_waste.common.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModeratableEntity extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ModerationStatus moderationStatus = ModerationStatus.VISIBLE;

  private Instant moderatedAt;

  @Column(name = "moderated_by")
  private UUID moderatedBy;

  public ModerationStatus getModerationStatus() {
    return moderationStatus;
  }

  public void setModerationStatus(ModerationStatus moderationStatus) {
    this.moderationStatus = moderationStatus;
  }

  public Instant getModeratedAt() {
    return moderatedAt;
  }

  public void setModeratedAt(Instant moderatedAt) {
    this.moderatedAt = moderatedAt;
  }

  public UUID getModeratedBy() {
    return moderatedBy;
  }

  public void setModeratedBy(UUID moderatedBy) {
    this.moderatedBy = moderatedBy;
  }

}
