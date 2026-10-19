package com.kamilpm.zero_waste.user.entity;

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
@Table(name = "user_bans")
public class UserBan {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "reason", nullable = false)
  private String reason;

  @Column(name = "banned_by", nullable = false)
  private UUID bannedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "revoked_by")
  private UUID revokedBy;

  @Column(name = "revoked_reason")
  private String revokedReason;

  public UserBan() {
  }

  public UserBan(UUID id, UUID userId, String reason, UUID bannedBy, Instant createdAt, Instant expiresAt,
      Instant revokedAt, UUID revokedBy, String revokedReason) {
    this.id = id;
    this.userId = userId;
    this.reason = reason;
    this.bannedBy = bannedBy;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.revokedAt = revokedAt;
    this.revokedBy = revokedBy;
    this.revokedReason = revokedReason;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public UUID getBannedBy() {
    return bannedBy;
  }

  public void setBannedBy(UUID bannedBy) {
    this.bannedBy = bannedBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public UUID getRevokedBy() {
    return revokedBy;
  }

  public void setRevokedBy(UUID revokedBy) {
    this.revokedBy = revokedBy;
  }

  public String getRevokedReason() {
    return revokedReason;
  }

  public void setRevokedReason(String revokedReason) {
    this.revokedReason = revokedReason;
  }

  @Override
  public String toString() {
    return "UserBan [id=" + id + ", userId=" + userId + ", reason=" + reason + ", bannedBy=" + bannedBy + ", createdAt="
        + createdAt + ", expiresAt=" + expiresAt + ", revokedAt=" + revokedAt + ", revokedBy=" + revokedBy
        + ", revokedReason=" + revokedReason + "]";
  }

}
