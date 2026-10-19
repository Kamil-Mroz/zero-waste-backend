package com.kamilpm.zero_waste.review.entity;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.UserVisibility;
import com.kamilpm.zero_waste.common.entity.ModeratableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
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

  @Column(name = "reviewer_visibility", nullable = false)
  @Enumerated(EnumType.STRING)
  private UserVisibility reviewerVisibility;

  public Review() {
  }

  public Review(UUID id, String comment, int rating, UUID offerId, UUID reviewerId, UUID revieweeId,
      UserVisibility reviewerVisibility) {
    this.id = id;
    this.comment = comment;
    this.rating = rating;
    this.offerId = offerId;
    this.reviewerId = reviewerId;
    this.revieweeId = revieweeId;
    this.reviewerVisibility = reviewerVisibility;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }

  public UUID getOfferId() {
    return offerId;
  }

  public void setOfferId(UUID offerId) {
    this.offerId = offerId;
  }

  public UUID getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(UUID reviewerId) {
    this.reviewerId = reviewerId;
  }

  public UUID getRevieweeId() {
    return revieweeId;
  }

  public void setRevieweeId(UUID revieweeId) {
    this.revieweeId = revieweeId;
  }

  public UserVisibility getReviewerVisibility() {
    return reviewerVisibility;
  }

  public void setReviewerVisibility(UserVisibility reviewerVisibility) {
    this.reviewerVisibility = reviewerVisibility;
  }

  @Override
  public String toString() {
    return "Review [id=" + id + ", comment=" + comment + ", rating=" + rating + ", offerId=" + offerId + ", reviewerId="
        + reviewerId + ", revieweeId=" + revieweeId + ", reviewerVisibility=" + reviewerVisibility + "]";
  }

}
