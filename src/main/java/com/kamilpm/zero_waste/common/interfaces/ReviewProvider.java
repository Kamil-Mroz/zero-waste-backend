package com.kamilpm.zero_waste.common.interfaces;

import java.util.UUID;

import com.kamilpm.zero_waste.common.dto.ProfileReviewSummary;

public interface ReviewProvider {

  public void reviewExists(UUID subjectId, UUID userId);

  public boolean isReviewerOrReviewee(UUID reviewId, UUID userId);

  public void deleteReviewById(UUID reviewId);

  public void hideReview(UUID adminId, UUID subjectId);

  public ProfileReviewSummary buildReviewSummary(UUID userId);

}
