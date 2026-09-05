package com.kamilpm.zero_waste.review.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kamilpm.zero_waste.common.entity.ModerationStatus;
import com.kamilpm.zero_waste.common.exception.EntityNotFoundException;
import com.kamilpm.zero_waste.common.exception.ForbiddenException;
import com.kamilpm.zero_waste.review.entity.Review;
import com.kamilpm.zero_waste.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewReportApi {
  private final ReviewRepository reviewRepository;

  public void reviewExists(UUID subjectId, UUID userId) {
    Review review = reviewRepository.findById(subjectId)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));

    if (Objects.equals(review.getReviewerId(), userId))
      throw new ForbiddenException("You can not report yourself");

    if (!Objects.equals(review.getRevieweeId(), userId))
      throw new ForbiddenException("You can not report review not received");

    if (Objects.equals(review.getModerationStatus(), ModerationStatus.HIDDEN))
      throw new ForbiddenException("Unable to report a hidden review");

  }

  public boolean isReviewerOrReviewee(UUID reviewId, UUID userId) {
    return reviewRepository.isReviewerOrReviewee(
        reviewId,
        userId);
  }

  public void deleteReviewById(UUID reviewId) {
    reviewRepository.deleteById(reviewId);
  }

  public void hideReview(UUID adminId, UUID subjectId) {

    Review review = reviewRepository.findByIdAndModerationStatus(subjectId, ModerationStatus.VISIBLE)
        .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    review.setModeratedAt(Instant.now());
    review.setModeratedBy(adminId);
    review.setModerationStatus(ModerationStatus.HIDDEN);
    reviewRepository.save(review);

  }

}
