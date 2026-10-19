package com.kamilpm.zero_waste.offer.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.kamilpm.zero_waste.common.dto.OfferStatus;
import com.kamilpm.zero_waste.common.dto.UserVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "offers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "buyer_id",
        "item_id"
    })
})

public class Offer {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "buyer_id", nullable = false)
  private UUID buyerId;

  @Column(name = "item_id", nullable = false)
  private UUID itemId;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private OfferStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "buyer_visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserVisibility buyerVisibility;

  public Offer() {
  }

  public Offer(UUID id, UUID buyerId, UUID itemId, OfferStatus status, Instant createdAt, Instant updatedAt,
      UserVisibility buyerVisibility) {
    this.id = id;
    this.buyerId = buyerId;
    this.itemId = itemId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.buyerVisibility = buyerVisibility;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getBuyerId() {
    return buyerId;
  }

  public void setBuyerId(UUID buyerId) {
    this.buyerId = buyerId;
  }

  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  public OfferStatus getStatus() {
    return status;
  }

  public void setStatus(OfferStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public UserVisibility getBuyerVisibility() {
    return buyerVisibility;
  }

  public void setBuyerVisibility(UserVisibility buyerVisibility) {
    this.buyerVisibility = buyerVisibility;
  }

}
