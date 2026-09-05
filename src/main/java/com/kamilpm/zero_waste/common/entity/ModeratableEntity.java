package com.kamilpm.zero_waste.common.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class ModeratableEntity extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ModerationStatus moderationStatus = ModerationStatus.VISIBLE;

  private Instant moderatedAt;

  @Column(name = "moderated_by")
  private UUID moderatedBy;

}
