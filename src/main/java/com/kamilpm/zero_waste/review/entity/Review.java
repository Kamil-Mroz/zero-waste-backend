package com.kamilpm.zero_waste.review.entity;

import java.util.UUID;

import com.kamilpm.zero_waste.common.entity.ModeratableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "reviews")
public class Review extends ModeratableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "description", nullable = true)
  private String comment;

  @Column(name = "rating", nullable = false)
  private int rating;

  @Column(name = "offer_id", nullable = false)
  private UUID offerId;

  @Column(name = "reviewer_id", nullable = false)
  private UUID reviewerId;

  @Column(name = "reviewee_id", nullable = false)
  private UUID revieweeId;

}
