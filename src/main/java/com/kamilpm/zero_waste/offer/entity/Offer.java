package com.kamilpm.zero_waste.offer.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
